import { useState } from "react";

const key = "vrs.user";

function readUser() {
  try {
    const user = JSON.parse(sessionStorage.getItem(key));
    return user?.id &&
      typeof user.name === "string" &&
      ["OWNER", "RTO_OFFICER", "RTO_ADMIN", "SYSTEM_ADMIN"].includes(user.role)
      ? { id: user.id, name: user.name, role: user.role }
      : null;
  } catch {
    return null;
  }
}

export default function useSessionUser() {
  const [user, setUser] = useState(readUser);
  function updateUser(account) {
    if (account) {
      const { id, name, role } = account;
      sessionStorage.setItem(key, JSON.stringify({ id, name, role }));
    } else {
      sessionStorage.removeItem(key);
    }
    setUser(account);
  }
  return [user, updateUser];
}
