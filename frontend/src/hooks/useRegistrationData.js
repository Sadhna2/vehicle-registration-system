import { useEffect, useState } from "react";
import { api } from "../api";

const empty = { content: [], totalElements: 0, totalPages: 0, vehicles: [] };

export default function useRegistrationData(user, page, refresh) {
  const [data, setData] = useState(empty);
  const [error, setError] = useState("");
  const [loaded, setLoaded] = useState(false);
  const ownerId = user?.role === "OWNER" ? user.id : null;
  const userId = user?.id;
  useEffect(() => {
    let active = true;
    setData(empty);
    setLoaded(false);
    setError("");
    if (!userId) return;
    async function load() {
      try {
        const [applications, vehicles] = await Promise.all([
          api(
            `/applications?page=${page}${ownerId ? `&ownerId=${ownerId}` : ""}`,
          ),
          ownerId ? api(`/vehicles?ownerId=${ownerId}`) : Promise.resolve([]),
        ]);
        if (active) {
          setData({ ...applications, vehicles });
          setLoaded(true);
          setError("");
        }
      } catch (failure) {
        if (active) setError(failure.message);
      }
    }
    load();
    const timer = setInterval(() => {
      if (!document.hidden) load();
    }, 15000);
    return () => {
      active = false;
      clearInterval(timer);
    };
  }, [userId, ownerId, page, refresh]);
  return { ...data, loaded, error, clearError: () => setError("") };
}
