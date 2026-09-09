/**
 * Cross-screen navigation coordination for the slide-out sidebar.
 *
 * When the sidebar navigates to a screen (Settings, Projects, MCP servers,
 * ...), it closes itself and records a pending return. When the user then
 * leaves that screen — via the header back button or the hardware back
 * gesture — the main screen consumes the pending flag and re-opens the
 * sidebar, so back navigation lands on the open sidebar instead of the
 * collapsed main page.
 *
 * Author: AjiroDesu
 */

let sidebarReturnPending = false;

/** Record that the next back navigation should re-open the sidebar. */
export function markSidebarReturnPending(): void {
  sidebarReturnPending = true;
}

/** Consume the pending flag: returns true once, then resets it. */
export function consumeSidebarReturnPending(): boolean {
  const pending = sidebarReturnPending;
  sidebarReturnPending = false;
  return pending;
}
