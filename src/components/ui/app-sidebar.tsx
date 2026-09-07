/**
 * Ajiro Agent sidebar — AMOLED-black redesign.
 * Structure adapted from the reference sidebar layout (top bar with app name +
 * search + new-chat affordances, primary nav group, Pinned/Recents history,
 * floating circular footer buttons) restyled to the Ajiro black palette.
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
  Brain,
  CalendarClock,
  EllipsisVertical,
  Library,
  Pause,
  Pencil,
  Pin,
  PinOff,
  Search,
  Server,
  Settings,
  SquarePen,
  Trash2,
  X,
} from "lucide-react-native";
import { useState } from "react";
import { ActivityIndicator, Pressable, Text, View } from "react-native";

import type { Conversation } from "@/core/types/app-state";
import { cn } from "@/core/utils";
import { useTheme } from "@/hooks/use-theme";

const NAV_ITEMS: {
  label: string;
  route: string;
  icon: typeof Library;
}[] = [
  { label: "Library", route: "/library", icon: Library },
  { label: "Scheduled", route: "/settings/jobs", icon: CalendarClock },
  { label: "Skills", route: "/settings/skills", icon: Brain },
  { label: "Plugins", route: "/settings/mcp", icon: Server },
];

export function AppSidebar() {
  const theme = useTheme();
  const pathname = usePathname();
  const router = useRouter();
  const { hydrating } = useAppState();
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
  const [renameTarget, setRenameTarget] = useState<Conversation | null>(null);
  const [renameTitle, setRenameTitle] = useState("");
  const [renameError, setRenameError] = useState<string | null>(null);
  const [renaming, setRenaming] = useState(false);

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
              <Text
                className={cn(
                  "min-w-0 flex-1 font-sans text-sm font-medium",
                  "text-foreground dark:text-foreground-dark",
                )}
                numberOfLines={1}
              >
                {conversation.title}
              </Text>
              <View className="shrink-0 items-center justify-center">
                {runStatusByConversation[conversation.id] === "running" ||
                runStatusByConversation[conversation.id] === "queued" ||
                runStatusByConversation[conversation.id] === "resumable" ? (
                  <ActivityIndicator
                    color={theme.textSecondary}
                    size="small"
                  />
                ) : runStatusByConversation[conversation.id] ===
                    "waiting_for_approval" ||
                  runStatusByConversation[conversation.id] ===
                    "waiting_for_question" ? (
                  <Pause color={theme.textSecondary} size={14} />
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
        <SidebarHeader className="min-h-8 flex-row items-center justify-between">
          <Text className="font-sans text-2xl font-semibold text-foreground dark:text-foreground-dark">
            Ajiro Agent
          </Text>
          <View className="flex-row items-center gap-sp-1">
            <Button
              accessibilityLabel="Search chats"
              onPress={() => {
                setSearchOpen((current) => !current);
                setSearchQuery("");
              }}
              size="icon"
              variant="ghost"
            >
              {searchOpen ? (
                <X color={theme.text} size={20} />
              ) : (
                <Search color={theme.text} size={20} />
              )}
            </Button>
            <SidebarClose asChild>
              <Button
                accessibilityLabel="New chat"
                onPress={openNewChat}
                size="icon"
                variant="ghost"
              >
                <SquarePen color={theme.text} size={20} />
              </Button>
            </SidebarClose>
          </View>
        </SidebarHeader>
        <SidebarContent>
          {searchOpen ? (
            <View className="pb-sp-1">
              <Input
                accessibilityLabel="Search chats"
                autoFocus
                onChangeText={setSearchQuery}
                placeholder="Search chats…"
                value={searchQuery}
              />
            </View>
          ) : null}

          <SidebarGroup className="pb-sp-2">
            <SidebarMenu className="gap-0">
              {NAV_ITEMS.map((item) => {
                const active =
                  pathname === item.route || pathname.startsWith(`${item.route}/`);

                return (
                  <SidebarMenuItem key={item.route}>
                    <SidebarClose asChild>
                      <SidebarMenuButton
                        fullBleed
                        isActive={active}
                        leftIcon={
                          <item.icon
                            color={theme.text}
                            size={22}
                            strokeWidth={2}
                          />
                        }
                        onPress={() => {
                          router.push(item.route as never);
                        }}
                      >
                        <Text className="font-sans text-base font-semibold text-foreground dark:text-foreground-dark">
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
            <SidebarGroup>
              <SidebarGroupLabel className="!px-0 text-sm font-semibold normal-case tracking-normal text-foreground dark:text-foreground-dark">
                Pinned
              </SidebarGroupLabel>
              <SidebarMenu>
                {pinnedConversations.map(renderConversation)}
              </SidebarMenu>
            </SidebarGroup>
          ) : null}
          <SidebarGroup>
            <SidebarGroupLabel className="!px-0 text-sm font-semibold normal-case tracking-normal text-foreground dark:text-foreground-dark">
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
        <SidebarFooter>
          {/* No divider — floating buttons sit flush against the content. */}
          <View className="flex-row items-center justify-between">
            <FloatingCircleButton
              accessibilityLabel="New chat"
              onPress={openNewChat}
            >
              <SquarePen color={theme.text} size={22} />
            </FloatingCircleButton>
            <SidebarClose asChild>
              <FloatingCircleButton
                accessibilityLabel="Settings"
                onPress={() => {
                  router.push("/settings");
                }}
              >
                <Settings color={theme.text} size={22} />
              </FloatingCircleButton>
            </SidebarClose>
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

function FloatingCircleButton({
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
      className="h-13 w-13 items-center justify-center rounded-full bg-sidebar-element dark:bg-sidebar-element-dark"
      onPress={onPress}
      style={({ pressed }) => ({
        elevation: 6,
        height: 52,
        opacity: pressed ? 0.88 : 1,
        shadowColor: "#000000",
        shadowOffset: { height: 4, width: 0 },
        shadowOpacity: 0.55,
        shadowRadius: 8,
        width: 52,
      })}
    >
      {children}
    </Pressable>
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
