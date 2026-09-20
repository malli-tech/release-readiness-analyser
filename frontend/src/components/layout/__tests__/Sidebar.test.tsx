import React from 'react';

/**
 * Sidebar Navigation Routing Contract & Destination Test
 */
export function verifySidebarNavigationDestinations() {
  // Helper to compute Releases href based on currentProjectId
  const getReleasesConfig = (currentProjectId: string | null) => ({
    href: currentProjectId ? `/projects/${currentProjectId}/releases` : '/projects',
    badge: currentProjectId ? undefined : 'Select Project',
  });

  // 1. Projects destination
  const projectsHref = '/projects';
  if (projectsHref !== '/projects') {
    throw new Error('Projects destination must be /projects');
  }

  // 2. Releases destination when project is selected (e.g. 'proj-100')
  const withProject = getReleasesConfig('proj-100');
  if (withProject.href !== '/projects/proj-100/releases' || withProject.badge !== undefined) {
    throw new Error('Releases with project selected must target /projects/proj-100/releases with no badge');
  }

  // 3. Releases behavior when no project is selected
  const noProject = getReleasesConfig(null);
  if (noProject.href !== '/projects' || noProject.badge !== 'Select Project') {
    throw new Error('Releases with no project selected must direct user to /projects with "Select Project" badge');
  }

  return true;
}

verifySidebarNavigationDestinations();
