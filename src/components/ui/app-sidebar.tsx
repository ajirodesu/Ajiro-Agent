/**
 * Ajiro Agent sidebar — slide-out navigation drawer.
 * Near-black panel (#0D0D0D-#141414 equivalent via sidebar tokens), scrollable
 * content with sticky bottom bar (Chat / avatar / voice), thin right-edge
 * divider, five primary nav items, Pinned + Recents sections with an optional
 * unread badge on any row.
 *
 * Author: AjiroDesu
 */
import { Button } from "@/components/ui/button";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { Input } from "@/components/ui/input";
import {
  Modal,
  ModalBody,
  ModalContent,
  ModalDescription,
  ModalFooter,
  ModalHeader,
  ModalTitle,
} from "@/components/ui/modal";
import {
  Sidebar,
  SidebarClose,
  SidebarContent,
  SidebarFooter,
  SidebarGroup,
  SidebarGroupLabel,
  SidebarHeader,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
} from "@/components/ui/sidebar";
import { useAppState } from "@/hooks/use-app-state";
import { useChat } from "@/hooks/use-chat";
import { usePathname, useRouter } from "expo-router";
import {
  Activity,
  AudioLines,
  BookMarked,
  Check,
  ChevronDown,
  Clock,
  EllipsisVertical,
  FolderOpen,
  Images,
  Library,
  AtSign,
  MessageSquare,
  Pencil,
  Pin,
  PinOff,
  Search,
  SquarePen,
  Trash2,
  X,
} from "lucide-react-native";
import { useState } from "react";
import { ActivityIndicator, Alert, Pressable, Text, View } from "react-native";

import type { Conversation } from "@/core/types/app-state";
import { ACTIVE_AGENT_RUN_STATUSES } from "@/modules/runtime/run-manager";
import { useElapsedSeconds } from "@/components/ui/processing-status";
import { useTheme } from "@/hooks/use-theme";

const ACCENT_BLUE = "#3B82F6";

/**
 * Primary nav. "Images" and "Projects" map to real app destinations:
 * Images -> Library filtered to image files, Projects -> coding settings
 * (project folder/sandbox management). Library/Scheduled/Plugins map to their
 * existing screens. If the product later grows dedicated Images/Projects
 * screens, swap the routes here.
 */
const NAV_ITEMS: { label: string; route: string; icon: typeof Library }[] = [
  { label: "Images", route: "/library?category=images", icon: Images },
  { label: "Library", route: "/library", icon: Library },
  { label: "Projects", route: "/settings/coding", icon: FolderOpen },
  { label: "Scheduled", route: "/settings/jobs", icon: Clock },
  { label: "Plugins", route: "/settings/mcp", icon: AtSign },
];

export function AppSidebar() {
  const theme = useTheme();
  const pathname = usePathname();
  const router = useRouter();
  const { agentRuns, hydrating } = useAppState();
  const {
    conversations,
    createConversation,
    currentConversation,
    renameConversation,
    runStatusByConversation,
    selectConversation,
  } = useChat();
  const [searchOpen, setSearchOpen] = useState(false);
  const [searchQuery, setSearchQuery] = useState("");
  const [tasksOpen, setTasksOpen] = useState(false);
  const [renameTarget, setRenameTarget] = useState<Conversation | null>(null);
  const [renameTitle, setRenameTitle] = useState("");
  const [renameError, setRenameError] = useState<string | null>(null);
  const [renaming, setRenaming] = useState(false);

  const activeRuns = agentRuns.filter((run) =>
    ACTIVE_AGENT_RUN_STATUSES.includes(run.status),
  );

  const query = searchQuery.trim().toLowerCase();
  const visibleConversations = query
    ? conversations.filter((conversation) =>
        conversation.title.toLowerCase().includes(query),
      )
    : conversations;

  const pinnedConversations = visibleConversations.filter(
    (conversation) => conversation.pinnedAt,
  );
  const otherConversations = visibleConversations.filter(
    (conversation) => !conversation.pinnedAt,
  );

  function renderConversation(conversation: (typeof conversations)[number]) {
    const active = conversation.id === currentConversation?.id;
    const runStatus = runStatusByConversation[conversation.id];
    const showBadge =
      runStatus === "running" ||
      runStatus === "queued" ||
      runStatus === "resumable" ||
      runStatus === "waiting_for_approval" ||
      runStatus === "waiting_for_question";

    return (
      <SidebarMenuItem key={conversation.id}>
        <SidebarClose asChild>
          <SidebarMenuButton
            fullBleed
            isActive={active}
            onPress={() => {
              selectConversation(conversation.id)
                .then(() => {
                  router.push("/");
                })
                .catch(console.error);
            }}
          >
            <View className="min-w-0 flex-1 flex-row items-center gap-sp-2">
              {conversation.pinnedAt ? (
                <MessageSquare
                  color={theme.text}
                  size={22}
                  strokeWidth={1.75}
                />
              ) : null}
              <Text
                className="min-w-0 flex-1 font-sans text-lg text-foreground dark:text-foreground-dark"
                numberOfLines={1}
              >
                {conversation.title}
              </Text>
              <View className="shrink-0 items-center justify-center">
                {showBadge ? (
                  <View className="mr-1 h-2.5 w-2.5 rounded-full" style={{ backgroundColor: ACCENT_BLUE }} />
                ) : runStatus === "retrying" ? (
                  <ActivityIndicator color={theme.textSecondary} size="small" />
                ) : (
                  <ChatOptions
                    conversationId={conversation.id}
                    color={theme.textSecondary}
                    pinned={Boolean(conversation.pinnedAt)}
                    pinnedCount={conversations.filter((item) => item.pinnedAt).length}
                    onRename={() => {
                      setRenameTarget(conversation);
                      setRenameTitle(conversation.title);
                      setRenameError(null);
                    }}
                  />
                )}
              </View>
            </View>
          </SidebarMenuButton>
        </SidebarClose>
      </SidebarMenuItem>
    );
  }

  const submitRename = () => {
    if (!renameTarget || !renameTitle.trim() || renaming) {
      return;
    }

    setRenaming(true);
    setRenameError(null);
    renameConversation(renameTarget.id, renameTitle)
      .then(() => {
        setRenameTarget(null);
      })
      .catch((renameFailure) => {
        setRenameError(
          renameFailure instanceof Error
            ? renameFailure.message
            : "Could not rename this chat.",
        );
      })
      .finally(() => {
        setRenaming(false);
      });
  };

  const openNewChat = () => {
    createConversation()
      .then(() => {
        router.push("/");
      })
      .catch(console.error);
  };

  return (
    <>
      <Sidebar>
        <SidebarContent>
          <SidebarHeader className="min-h-14 flex-row items-center justify-between gap-sp-2 pb-sp-2">
            <Text
              className="min-w-0 flex-1 font-sans text-[30px] font-bold text-foreground dark:text-foreground-dark"
              numberOfLines={1}
            >
              Ajiro Agent
            </Text>
            <View className="shrink-0 flex-row items-center gap-sp-2">
              {activeRuns.length > 0 ? (
                <Pressable
                  accessibilityLabel={`${activeRuns.length} background tasks running`}
                  accessibilityRole="button"
                  className="h-14 w-14 items-center justify-center rounded-full"
                  onPress={() => {
                    setTasksOpen((current) => !current);
                  }}
                  style={({ pressed }) => ({
                    backgroundColor: "#2A2A2A",
                    opacity: pressed ? 0.85 : 1,
                  })}
                >
                  <View className="relative">
                    <Activity color={ACCENT_BLUE} size={24} strokeWidth={1.75} />
                    <View
                      className="absolute -right-2 -top-1 h-4 min-w-4 items-center justify-center rounded-pill px-1"
                      style={{ backgroundColor: ACCENT_BLUE }}
                    >
                      <Text className="font-sans text-[10px] font-bold text-white">
                        {activeRuns.length}
                      </Text>
                    </View>
                  </View>
                </Pressable>
              ) : null}
              <HeaderIconButton
                accessibilityLabel="Search chats"
                onPress={() => {
                  setSearchOpen((current) => !current);
                  setSearchQuery("");
                }}
              >
                <Search color={theme.text} size={24} strokeWidth={1.75} />
              </HeaderIconButton>
              <SidebarClose asChild>
                <HeaderIconButton accessibilityLabel="New chat" onPress={openNewChat}>
                  <MessageSquarePlusIcon />
                </HeaderIconButton>
              </SidebarClose>
            </View>
          </SidebarHeader>

          {searchOpen ? (
            <View className="pb-sp-2">
              <Input
                accessibilityLabel="Search chats"
                autoFocus
                onChangeText={setSearchQuery}
                placeholder="Search chats…"
                value={searchQuery}
              />
            </View>
          ) : null}

          {tasksOpen ? (
            <View className="mb-sp-2 gap-sp-1 rounded-ui border border-border p-sp-2 dark:border-border-dark">
              <View className="flex-row items-center justify-between px-1 pb-1">
                <Text className="font-sans text-sm font-bold text-muted-foreground dark:text-muted-foreground-dark">
                  Background tasks
                </Text>
                <Pressable
                  accessibilityLabel="Close background tasks"
                  accessibilityRole="button"
                  hitSlop={8}
                  onPress={() => {
                    setTasksOpen(false);
                  }}
                >
                  <X color={theme.textSecondary} size={16} />
                </Pressable>
              </View>
              {activeRuns.length > 0 ? (
                activeRuns.map((run) => (
                  <BackgroundTaskRow key={run.id} run={run} />
                ))
              ) : (
                <Text className="px-1 pb-1 font-sans text-xs text-muted-foreground dark:text-muted-foreground-dark">
                  No active tasks.
                </Text>
              )}
            </View>
          ) : null}

          <SidebarGroup className="pb-sp-1">
            <SidebarMenu className="gap-0">
              {NAV_ITEMS.map((item) => {
                const basePath = item.route.split("?")[0];
                const active =
                  pathname === basePath || pathname.startsWith(`${basePath}/`);

                return (
                  <SidebarMenuItem key={item.route}>
                    <SidebarClose asChild>
                      <SidebarMenuButton
                        fullBleed
                        isActive={active}
                        leftIcon={
                          <item.icon color={theme.text} size={26} strokeWidth={1.75} />
                        }
                        onPress={() => {
                          router.push(item.route as never);
                        }}
                      >
                        <Text className="font-sans text-xl font-medium text-foreground dark:text-foreground-dark">
                          {item.label}
                        </Text>
                      </SidebarMenuButton>
                    </SidebarClose>
                  </SidebarMenuItem>
                );
              })}
            </SidebarMenu>
          </SidebarGroup>

          {pinnedConversations.length > 0 ? (
            <SidebarGroup className="pt-sp-4">
              <SidebarGroupLabel className="!px-0 text-base font-bold normal-case tracking-normal text-muted-foreground dark:text-muted-foreground-dark">
                Pinned
              </SidebarGroupLabel>
              <SidebarMenu>
                {pinnedConversations.map(renderConversation)}
              </SidebarMenu>
            </SidebarGroup>
          ) : null}

          <SidebarGroup className="pt-sp-4 pb-32">
            <SidebarGroupLabel className="!px-0 text-base font-bold normal-case tracking-normal text-muted-foreground dark:text-muted-foreground-dark">
              Recents
            </SidebarGroupLabel>
            <SidebarMenu>
              {otherConversations.map(renderConversation)}
              {visibleConversations.length === 0 ? (
                <SidebarMenuItem>
                  {hydrating ? (
                    <View className="flex-row items-center gap-sp-2 px-sp-2 py-sp-2">
                      <ActivityIndicator
                        color={theme.textSecondary}
                        size="small"
                      />
                      <Text className="font-sans text-sm text-muted-foreground dark:text-muted-foreground-dark">
                        Loading chats…
                      </Text>
                    </View>
                  ) : (
                    <Text className="px-sp-2 font-sans text-sm text-muted-foreground dark:text-muted-foreground-dark">
                      {query
                        ? "No chats match your search."
                        : "No chats yet. Start a new conversation."}
                    </Text>
                  )}
                </SidebarMenuItem>
              ) : null}
            </SidebarMenu>
          </SidebarGroup>
        </SidebarContent>

        <SidebarFooter
          className="z-10 bg-sidebar dark:bg-sidebar-dark"
          style={{
            paddingBottom: 12,
          }}
        >
          <View className="flex-row items-center justify-between px-sp-1">
            <SidebarClose asChild>
              <Pressable
                accessibilityLabel="New chat"
                accessibilityRole="button"
                className="flex-row items-center gap-2 rounded-pill"
                onPress={openNewChat}
                style={({ pressed }) => ({
                  backgroundColor: pressed ? "#2563EB" : ACCENT_BLUE,
                  paddingHorizontal: 22,
                  paddingVertical: 14,
                })}
              >
                <SquarePen color="#FFFFFF" size={18} strokeWidth={2.25} />
                <Text className="font-sans text-base font-bold text-white">
                  Chat
                </Text>
              </Pressable>
            </SidebarClose>

            <Pressable
              accessibilityLabel="Account"
              accessibilityRole="button"
              className="h-16 w-16 items-center justify-center overflow-hidden rounded-full"
              onPress={() => {
                router.push("/settings");
              }}
              style={({ pressed }) => ({ opacity: pressed ? 0.85 : 1 })}
            >
              <View
                className="h-16 w-16 items-center justify-center rounded-full"
                style={{ backgroundColor: "#2A2A2A" }}
              >
                <BookMarked color="#FFFFFF" size={24} strokeWidth={1.75} />
              </View>
            </Pressable>

            <Pressable
              accessibilityLabel="Voice mode"
              accessibilityRole="button"
              className="h-16 w-16 items-center justify-center rounded-full"
              onPress={() => {
                Alert.alert(
                  "Voice mode",
                  "Voice conversations are coming soon.",
                );
              }}
              style={({ pressed }) => ({ opacity: pressed ? 0.85 : 1 })}
            >
              <View
                className="h-16 w-16 items-center justify-center rounded-full"
                style={{ backgroundColor: "#2A2A2A" }}
              >
                <View
                  className="h-12 w-12 items-center justify-center rounded-full"
                  style={{ backgroundColor: "#172554" }}
                >
                  <AudioLines color={ACCENT_BLUE} size={24} strokeWidth={2} />
                </View>
              </View>
            </Pressable>
          </View>
        </SidebarFooter>
      </Sidebar>
      <Modal
        dismissible={!renaming}
        onOpenChange={(open) => {
          if (!open && !renaming) {
            setRenameTarget(null);
            setRenameError(null);
          }
        }}
        open={renameTarget !== null}
      >
        <ModalContent>
          <ModalHeader>
            <ModalTitle>Rename chat</ModalTitle>
            <ModalDescription>
              Choose a title that makes this chat easy to find.
            </ModalDescription>
          </ModalHeader>
          <ModalBody>
            <Input
              accessibilityLabel="Chat title"
              autoFocus
              maxLength={80}
              onChangeText={setRenameTitle}
              onSubmitEditing={submitRename}
              returnKeyType="done"
              selectTextOnFocus
              value={renameTitle}
            />
            {renameError ? (
              <Text className="font-sans text-sm text-destructive dark:text-destructive-dark">
                {renameError}
              </Text>
            ) : null}
          </ModalBody>
          <ModalFooter>
            <Button
              disabled={renaming}
              onPress={() => {
                setRenameTarget(null);
                setRenameError(null);
              }}
              size="sm"
              variant="ghost"
            >
              Cancel
            </Button>
            <Button
              disabled={!renameTitle.trim()}
              loading={renaming}
              onPress={submitRename}
              size="sm"
            >
              Rename
            </Button>
          </ModalFooter>
        </ModalContent>
      </Modal>
    </>
  );
}

function BackgroundTaskRow({
  run,
}: {
  run: {
    conversationId: string;
    input: string;
    startedAt: string;
    status: string;
  };
}) {
  const theme = useTheme();
  const { conversations, selectConversation } = useChat();
  const router = useRouter();
  const elapsed = useElapsedSeconds(run.startedAt, true);
  const conversation = conversations.find(
    (candidate) => candidate.id === run.conversationId,
  );
  const isDone = run.status === "completed";
  const isFailed = run.status === "failed" || run.status === "canceled";

  return (
    <Pressable
      accessibilityRole="button"
      className="flex-row items-center gap-sp-2 rounded-ui px-sp-2 py-sp-2"
      onPress={() => {
        selectConversation(run.conversationId)
          .then(() => {
            router.push("/");
          })
          .catch(console.error);
      }}
      style={({ pressed }) => (pressed ? { opacity: 0.8 } : null)}
    >
      <View className="w-5 items-center">
        {isDone ? (
          <Check color={theme.textSecondary} size={14} />
        ) : isFailed ? (
          <X color={theme.destructive} size={14} />
        ) : (
          <ActivityIndicator color={theme.textSecondary} size="small" />
        )}
      </View>
      <Text
        className="min-w-0 flex-1 font-sans text-sm text-foreground dark:text-foreground-dark"
        numberOfLines={1}
      >
        {run.input || conversation?.title || "Task"}
      </Text>
      <Text className="font-mono text-xs text-muted-foreground dark:text-muted-foreground-dark">
        {elapsed}s
      </Text>
    </Pressable>
  );
}

function HeaderIconButton({
  accessibilityLabel,
  children,
  onPress,
}: {
  accessibilityLabel: string;
  children: React.ReactNode;
  onPress: () => void;
}) {
  return (
    <Pressable
      accessibilityLabel={accessibilityLabel}
      accessibilityRole="button"
      className="h-14 w-14 items-center justify-center rounded-full"
      onPress={onPress}
      style={({ pressed }) => ({
        backgroundColor: "#2A2A2A",
        opacity: pressed ? 0.85 : 1,
      })}
    >
      {children}
    </Pressable>
  );
}

function MessageSquarePlusIcon() {
  // Chat-bubble outline with a small pencil overlay, per the reference.
  return (
    <View className="items-center justify-center">
      <MessageSquare color="#FFFFFF" size={24} strokeWidth={1.75} />
      <View className="absolute -bottom-0.5 -right-1 h-3 w-3 items-center justify-center rounded-full" style={{ backgroundColor: "#2A2A2A" }}>
        <Pencil color="#FFFFFF" size={8} strokeWidth={2.5} />
      </View>
    </View>
  );
}

function ChatOptions({
  color,
  conversationId,
  onRename,
  pinned,
  pinnedCount,
}: {
  color: string;
  conversationId: string;
  onRename: () => void;
  pinned: boolean;
  pinnedCount: number;
}) {
  const { deleteConversation, setConversationPinned } = useChat();
  const theme = useTheme();
  return (
    <DropdownMenu>
      <DropdownMenuTrigger>
        <Pressable hitSlop={8}>
          <EllipsisVertical size={20} color={color} />
        </Pressable>
      </DropdownMenuTrigger>

      <DropdownMenuContent width={190}>
        <DropdownMenuItem onPress={onRename}>
          <View className="flex-row items-center gap-sp-2">
            <Pencil color={theme.text} size={16} />
            <Text className="font-sans text-base text-foreground dark:text-foreground-dark">
              Rename
            </Text>
          </View>
        </DropdownMenuItem>
        <DropdownMenuItem
          disabled={!pinned && pinnedCount >= 3}
          onPress={() => {
            setConversationPinned(conversationId, !pinned).catch(console.error);
          }}
        >
          <View className="flex-row items-center gap-sp-2">
            {pinned ? (
              <PinOff color={theme.text} size={16} />
            ) : (
              <Pin color={theme.text} size={16} />
            )}
            <Text className="font-sans text-base text-foreground dark:text-foreground-dark">
              {pinned
                ? "Unpin"
                : pinnedCount >= 3
                  ? "Pin limit reached"
                  : "Pin"}
            </Text>
          </View>
        </DropdownMenuItem>
        <DropdownMenuItem
          onPress={() => {
            deleteConversation(conversationId).catch(console.error);
          }}
        >
          <View className="flex-row items-center gap-sp-2">
            <Trash2 color={theme.destructive} size={16} />
            <Text className="font-sans text-base text-destructive dark:text-destructive-dark">
              Delete
            </Text>
          </View>
        </DropdownMenuItem>
      </DropdownMenuContent>
    </DropdownMenu>
  );
}
