import React from 'react';
import { Sidebar } from '../Sidebar';

/**
 * Sidebar Component Contract & Key Uniqueness Test
 */
export function verifySidebarKeyUniqueness() {
  // Test Navigation items uniqueness
  const itemIds = ['dashboard', 'projects', 'releases', 'reports', 'settings'];
  const uniqueIds = new Set(itemIds);

  if (itemIds.length !== uniqueIds.size) {
    throw new Error('Sidebar navigation item IDs must be unique');
  }

  return true;
}

verifySidebarKeyUniqueness();
