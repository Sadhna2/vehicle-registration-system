import { useEffect, useState } from "react";
import { api } from "../api";

const empty = { applications: [], vehicles: [] };

export default function useRegistrationData(user, refresh) {
  const [data, setData] = useState(empty);
  const [error, setError] = useState("");
  const [loaded, setLoaded] = useState(false);
  const owner = user?.role === "OWNER";
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
          api("/applications"),
          owner ? api("/vehicles") : Promise.resolve([]),
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
  }, [userId, owner, refresh]);
  return { ...data, loaded, error, clearError: () => setError("") };
}
