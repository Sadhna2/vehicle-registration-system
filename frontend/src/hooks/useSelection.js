import { useCallback, useMemo, useState } from "react";

// Tracks which table rows are ticked for a bulk action.
// `visibleIds` are the ids currently shown, so "select all" follows any filter.
export default function useSelection(visibleIds) {
  const [selected, setSelected] = useState(() => new Set());
  const visible = useMemo(() => new Set(visibleIds.map(String)), [visibleIds]);
  // Only count ticks on rows that are still shown (rows can disappear after a refresh or filter).
  const ids = useMemo(() => [...selected].filter((id) => visible.has(id)), [selected, visible]);

  const toggle = useCallback((id) => {
    setSelected((current) => {
      const next = new Set(current);
      const key = String(id);
      if (next.has(key)) next.delete(key);
      else next.add(key);
      return next;
    });
  }, []);

  const allSelected = visible.size > 0 && ids.length === visible.size;
  const toggleAll = useCallback(() => {
    setSelected(allSelected ? new Set() : new Set(visible));
  }, [allSelected, visible]);

  const clear = useCallback(() => setSelected(new Set()), []);

  return {
    ids,
    count: ids.length,
    has: (id) => selected.has(String(id)),
    toggle,
    toggleAll,
    allSelected,
    someSelected: ids.length > 0 && !allSelected,
    clear,
  };
}
