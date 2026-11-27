import { useCallback, useEffect, useState } from "react";
import { clearSession, readSession, saveSession, SESSION_EVENT } from "../session";

/**
 * The signed-in user ({ id, name, role }) taken from the stored JWT session.
 * The session is cleared when the token expires or the API rejects it (401).
 */
export default function useSessionUser() {
  const [user, setUser] = useState(() => readSession()?.user ?? null);

  useEffect(() => {
    const sync = () => setUser(readSession()?.user ?? null);
    window.addEventListener(SESSION_EVENT, sync);
    return () => window.removeEventListener(SESSION_EVENT, sync);
  }, []);

  const updateUser = useCallback((authentication) => {
    if (authentication) saveSession(authentication);
    else clearSession();
  }, []);

  return [user, updateUser];
}
