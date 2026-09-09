import { Redirect } from "expo-router";

/** Legacy route - the dedicated manager now lives at /settings/mcp. */
export default function ConnectedMcpServersScreen() {
  return <Redirect href={"/settings/mcp" as never} />;
}
