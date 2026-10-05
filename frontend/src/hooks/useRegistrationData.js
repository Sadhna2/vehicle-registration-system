import { useEffect, useState } from "react";
import { api } from "../api";

const empty = { applications: [], vehicles: [] };

export default function useRegistrationData(user, refresh) {
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
          api(`/applications${ownerId ? `?ownerId=${ownerId}` : ""}`),
          ownerId ? api(`/vehicles?ownerId=${ownerId}`) : Promise.resolve([]),
        ]);
        if (active) {
          setData({ applications, vehicles });
          setLoaded(true);
          setError("");
        }
      } catch (failure) {
        if (active) setError(failure.message);
      }
    }
    load();
    return () => {
      active = false;
    };
  }, [userId, ownerId, refresh]);
  return { ...data, loaded, error, clearError: () => setError("") };
}
