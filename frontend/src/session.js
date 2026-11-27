// Stores the JWT session for this browser tab. sessionStorage is cleared when the tab closes.
const KEY = "vrs.session";
export const SESSION_EVENT = "vrs-session-changed";
const ROLES = ["OWNER", "RTO_OFFICER", "RTO_ADMIN", "SYSTEM_ADMIN"];

export function readSession() {
  try {
    const session = JSON.parse(sessionStorage.getItem(KEY));
    const valid =
      typeof session?.token === "string" &&
      session.user?.id &&
      typeof session.user.name === "string" &&
      ROLES.includes(session.user.role) &&
      Date.parse(session.expiresAt) > Date.now();
    if (!valid) {
      if (session) sessionStorage.removeItem(KEY);
      return null;
    }
    return session;
  } catch {
    return null;
  }
}

export function saveSession({ accessToken, expiresAt, user }) {
  const { id, name, role } = user;
  sessionStorage.setItem(
    KEY,
    JSON.stringify({ token: accessToken, expiresAt, user: { id, name, role } }),
  );
  window.dispatchEvent(new Event(SESSION_EVENT));
}

export function clearSession() {
  sessionStorage.removeItem(KEY);
  window.dispatchEvent(new Event(SESSION_EVENT));
}

export function sessionToken() {
  return readSession()?.token ?? null;
}
