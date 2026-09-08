/**
 * Coding harness settings screen: exec allow-list, verify loop, and default
 * approval mode for coding sessions.
 *
 * Author: AjiroDesu
 */
import { useRouter } from "expo-router";
import { ChevronLeft } from "lucide-react-native";
import { useState } from "react";
import { Pressable, Switch, Text, View } from "react-native";
import { Container } from "@/components/shared/container";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Separator } from "@/components/ui/separator";
import {
  EXEC_COMMAND_DESCRIPTIONS,
  EXEC_COMMAND_IDS,
} from "@/modules/tools/coding/exec";
import { cn } from "@/core/utils";
import { useConfig } from "@/hooks/use-config";
import { useTheme } from "@/hooks/use-theme";
import type { CodingExecCommandId } from "@/core/services/coding/coding-settings";

function CheckRow({
  checked,
  description,
  disabled,
  label,
  onToggle,
}: {
  checked: boolean;
  description: string;
  disabled?: boolean;
  label: string;
  onToggle: () => void;
}) {
  return (
    <Pressable
      accessibilityRole="checkbox"
      accessibilityState={{ checked, disabled }}
      className={cn("gap-1 px-sp-4 py-sp-3", disabled && "opacity-50")}
      disabled={disabled}
      onPress={onToggle}
    >
      <Text className="font-sans text-base text-foreground dark:text-foreground-dark">
        {checked ? "?" : "?"} {label}
      </Text>
      <Text className="font-sans text-xs text-muted-foreground dark:text-muted-foreground-dark">
        {description}
      </Text>
    </Pressable>
  );
}

export default function CodingSettingsScreen() {
  const router = useRouter();
  const theme = useTheme();
  const { codingSettings, updateCodingSettings } = useConfig();
  const [saving, setSaving] = useState(false);

  const apply = (input: Partial<typeof codingSettings>) => {
    setSaving(true);
    updateCodingSettings(input)
      .catch(console.error)
      .finally(() => {
        setSaving(false);
      });
  };

  const toggleVerifyCommand = (command: CodingExecCommandId) => {
    const has = codingSettings.verifyCommands.includes(command);
    const next = has
      ? codingSettings.verifyCommands.filter((item) => item !== command)
      : [...codingSettings.verifyCommands, command];

    apply({ verifyCommands: next });
  };

  return (
    <Container contentClassName="gap-sp-4 py-sp-4" includeBottomTabInset={false}>
      <View className="flex-row items-center gap-sp-2">
        <Button
          leftIcon={<ChevronLeft color={theme.text} size={16} />}
          onPress={() => {
            router.push("/settings");
          }}
          size="icon-xs"
          variant="ghost"
        />
        <Text className="font-sans text-xl font-semibold text-foreground dark:text-foreground-dark">
          Coding
        </Text>
      </View>

      <Card className="overflow-hidden">
        <View className="flex-row items-center gap-sp-3 px-sp-4 py-sp-3">
          <View className="flex-1 gap-1">
            <Text className="font-sans text-base text-foreground dark:text-foreground-dark">
              Exec tool
            </Text>
            <Text className="font-sans text-xs text-muted-foreground dark:text-muted-foreground-dark">
              Allow-listed in-process checks (typecheck, lint, grep, stats).
              Android has no shell; nothing is executed outside the app sandbox.
            </Text>
          </View>
          <Switch
            accessibilityLabel="Enable exec tool"
            disabled={saving}
            onValueChange={(value) => {
              apply({ execEnabled: value });
            }}
            value={codingSettings.execEnabled}
          />
        </View>
        <Separator />
        <View className="flex-row items-center gap-sp-3 px-sp-4 py-sp-3">
          <View className="flex-1 gap-1">
            <Text className="font-sans text-base text-foreground dark:text-foreground-dark">
              Local git tools
            </Text>
            <Text className="font-sans text-xs text-muted-foreground dark:text-muted-foreground-dark">
              status, diff, add, commit, branch, log — via isomorphic-git on the
              granted project folder. Remote operations use GitHub MCP.
            </Text>
          </View>
          <Switch
            accessibilityLabel="Enable local git tools"
            disabled={saving}
            onValueChange={(value) => {
              apply({ gitEnabled: value });
            }}
            value={codingSettings.gitEnabled}
          />
        </View>
      </Card>

      <Card className="overflow-hidden">
        <View className="flex-row items-center gap-sp-3 px-sp-4 py-sp-3">
          <View className="flex-1 gap-1">
            <Text className="font-sans text-base text-foreground dark:text-foreground-dark">
              Verify loop
            </Text>
            <Text className="font-sans text-xs text-muted-foreground dark:text-muted-foreground-dark">
              After edits, run the selected checks and feed failures back to the
              model (up to {codingSettings.verifyMaxRetries} retries).
            </Text>
          </View>
          <Switch
            accessibilityLabel="Enable verify loop"
            disabled={saving}
            onValueChange={(value) => {
              apply({ verifyEnabled: value });
            }}
            value={codingSettings.verifyEnabled}
          />
        </View>
        <Separator />
        {EXEC_COMMAND_IDS.map((command, index) => (
          <View key={command}>
            {index > 0 ? <Separator /> : null}
            <CheckRow
              checked={codingSettings.verifyCommands.includes(command)}
              description={EXEC_COMMAND_DESCRIPTIONS[command]}
              disabled={saving || !codingSettings.verifyEnabled}
              label={command}
              onToggle={() => {
                toggleVerifyCommand(command);
              }}
            />
          </View>
        ))}
      </Card>

      <Card className="overflow-hidden">
        <View className="px-sp-4 py-sp-3">
          <Text className="font-sans text-base text-foreground dark:text-foreground-dark">
            Sandbox
          </Text>
          <Text className="mt-1 font-sans text-xs text-muted-foreground dark:text-muted-foreground-dark">
            Writable paths are limited to the folder granted per chat via
            Android&apos;s storage access framework. Network access is limited
            to model providers and connected MCP servers. Destructive tool calls
            follow the tool approval mode (Ask / Allow) on the chat screen.
          </Text>
        </View>
      </Card>
    </Container>
  );
}
