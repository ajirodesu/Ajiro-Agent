import type { SupportedProviderDefinition } from "@/modules/providers/types";

/**
 * Built-in OpenAI-compatible profiles. Each entry is pre-filled connection
 * details for the generic OpenAI-chat-completions adapter — nothing here is
 * special-cased; any custom provider the user adds through Settings uses the
 * exact same code path.
 */
const OPENAI_COMPATIBLE_PROFILES = [
  ["baseten", "Baseten", "https://inference.baseten.co/v1"],
  ["cerebras", "Cerebras", "https://api.cerebras.ai/v1"],
  ["deepinfra", "DeepInfra", "https://api.deepinfra.com/v1/openai"],
  ["deepseek", "DeepSeek", "https://api.deepseek.com/v1"],
  ["fireworks", "Fireworks AI", "https://api.fireworks.ai/inference/v1"],
  ["groq", "Groq", "https://api.groq.com/openai/v1"],
  ["opencode", "OpenCode Zen", "https://opencode.ai/zen/v1"],
  ["togetherai", "Together AI", "https://api.together.xyz/v1"],
  ["bai", "B AI", "https://api.b-ai.ai/v1"],
  ["unorouter", "UnoRouter", "https://api.unorouter.com/v1"],
  ["inceptionlabs", "InceptionLabs", "https://api.inceptionlabs.ai/v1"],
  ["qwencloud", "QwenCloud", "https://api.qwencloud.dev/v1"],
  ["tokenharbor", "TokenHarbor", "https://api.tokenharbor.io/v1"],
  ["grokified", "Grokified", "https://api.grokified.com/v1"],
  ["kiasapi", "KiasAPI", "https://api.kiasapi.dev/v1"],
  ["tensormux", "TensorMux", "https://api.tensormux.com/v1"],
  ["assemblyai", "AssemblyAI", "https://api.assemblyai.ai/v1"],
] as const;

export const OPENAI_COMPATIBLE_PROFILE_PROVIDERS =
  OPENAI_COMPATIBLE_PROFILES.map(([id, label, baseUrl]) => ({
    config: {
      id,
      family: "openai-compatible" as const,
      label,
      authType: "apiKey" as const,
      baseUrl,
      enabled: false,
      oauthAccountEmail: null,
    },
  })) satisfies SupportedProviderDefinition[];
