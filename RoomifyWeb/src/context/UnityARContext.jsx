import React, { createContext, useContext, useState, useCallback } from 'react';

/**
 * UnityARContext — Global bridge between React UI and the Unity WebGL AR engine.
 *
 * Flow:
 *   React (CatalogPage) → selectFurnitureForAR(item) → navigates to /ar
 *   ARPage renders <UnityARViewer /> → sends item to Unity via sendMessage
 *   Unity places furniture → fires dispatchReactUnityEvent('OnFurniturePlaced', json)
 *   UnityARViewer receives it → calls recordPlacement(data)
 *   ARPage shows placement list
 */

const UnityARContext = createContext(null);

export const UnityARProvider = ({ children }) => {
  // The furniture item the user wants to preview in AR
  const [selectedFurniture, setSelectedFurniture] = useState(null);

  // All items placed in the AR session so far
  const [placements, setPlacements] = useState([]);

  // Whether the AR viewer overlay is considered active
  const [isARActive, setIsARActive] = useState(false);

  // AR session status string ('idle' | 'loading' | 'running' | 'error')
  const [arStatus, setArStatus] = useState('idle');

  /**
   * Called from the Catalog (or any page) to queue a furniture item for AR.
   * Navigating to /ar is handled by the caller.
   */
  const selectFurnitureForAR = useCallback((item) => {
    setSelectedFurniture(item);
    setIsARActive(true);
  }, []);

  /**
   * Called by UnityARViewer when it receives an OnFurniturePlaced event from Unity.
   */
  const recordPlacement = useCallback((placementData) => {
    setPlacements((prev) => [
      ...prev,
      { ...placementData, timestamp: Date.now() },
    ]);
  }, []);

  /**
   * Clear all placements (e.g. on session reset).
   */
  const clearPlacements = useCallback(() => {
    setPlacements([]);
  }, []);

  return (
    <UnityARContext.Provider
      value={{
        selectedFurniture,
        setSelectedFurniture,
        placements,
        isARActive,
        setIsARActive,
        arStatus,
        setArStatus,
        selectFurnitureForAR,
        recordPlacement,
        clearPlacements,
      }}
    >
      {children}
    </UnityARContext.Provider>
  );
};

export const useUnityAR = () => {
  const ctx = useContext(UnityARContext);
  if (!ctx) {
    throw new Error('useUnityAR must be used inside <UnityARProvider>');
  }
  return ctx;
};
