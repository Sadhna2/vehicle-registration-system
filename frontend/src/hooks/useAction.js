import { useState } from "react";

export default function useAction() {
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  async function run(task) {
    setBusy(true);
    setError("");
    try {
      await task();
    } catch (failure) {
      setError(failure.message);
    } finally {
      setBusy(false);
    }
  }
  return { run, busy, error, setError };
}
