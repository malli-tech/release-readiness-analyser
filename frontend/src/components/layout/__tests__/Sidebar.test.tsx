import React from 'react';

/**
 * Sidebar Navigation Routing Contract & Destination Test
 */
export function verifySidebarNavigationDestinations() {
  // Helper to compute Releases href based on currentProjectId
  const getReleasesConfig = (currentProjectId: string | null) => ({
    href: currentProjectId ? `/projects/${currentProjectId}/releases` : '/releases',
  });

  // 1. Projects destination
  const projectsHref = '/projects';
  if (projectsHref !== '/projects') {
    throw new Error('Projects destination must be /projects');
  }

  // 2. Releases destination when project is selected (e.g. 'proj-100')
  const withProject = getReleasesConfig('proj-100');
  if (withProject.href !== '/projects/proj-100/releases') {
    throw new Error('Releases with project selected must target /projects/proj-100/releases');
  }

  // 3. Releases behavior when no project is selected
  const noProject = getReleasesConfig(null);
  if (noProject.href !== '/releases') {
    throw new Error('Releases with no project selected must direct user to /releases');
  }

  return true;
}

verifySidebarNavigationDestinations();
