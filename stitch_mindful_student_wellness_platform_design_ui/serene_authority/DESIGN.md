---
name: Serene Authority
colors:
  surface: '#f8f9ff'
  surface-dim: '#cbdbf5'
  surface-bright: '#f8f9ff'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#eff4ff'
  surface-container: '#e5eeff'
  surface-container-high: '#dce9ff'
  surface-container-highest: '#d3e4fe'
  on-surface: '#0b1c30'
  on-surface-variant: '#44474e'
  inverse-surface: '#213145'
  inverse-on-surface: '#eaf1ff'
  outline: '#75777e'
  outline-variant: '#c5c6cf'
  surface-tint: '#4e5e80'
  primary: '#031634'
  on-primary: '#ffffff'
  primary-container: '#1a2b4a'
  on-primary-container: '#8293b7'
  inverse-primary: '#b6c6ee'
  secondary: '#006b5f'
  on-secondary: '#ffffff'
  secondary-container: '#7bf7e3'
  on-secondary-container: '#007165'
  tertiary: '#0e1918'
  on-tertiary: '#ffffff'
  tertiary-container: '#222d2c'
  on-tertiary-container: '#899593'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#d8e2ff'
  primary-fixed-dim: '#b6c6ee'
  on-primary-fixed: '#081b39'
  on-primary-fixed-variant: '#364767'
  secondary-fixed: '#7bf7e3'
  secondary-fixed-dim: '#5cdac7'
  on-secondary-fixed: '#00201c'
  on-secondary-fixed-variant: '#005047'
  tertiary-fixed: '#d9e5e3'
  tertiary-fixed-dim: '#bdc9c7'
  on-tertiary-fixed: '#131d1d'
  on-tertiary-fixed-variant: '#3e4948'
  background: '#f8f9ff'
  on-background: '#0b1c30'
  surface-variant: '#d3e4fe'
typography:
  h1:
    fontFamily: Inter
    fontSize: 32px
    fontWeight: '600'
    lineHeight: '1.2'
    letterSpacing: -0.02em
  h2:
    fontFamily: Inter
    fontSize: 24px
    fontWeight: '600'
    lineHeight: '1.3'
    letterSpacing: -0.01em
  h3:
    fontFamily: Inter
    fontSize: 20px
    fontWeight: '600'
    lineHeight: '1.4'
    letterSpacing: '0'
  body-lg:
    fontFamily: Inter
    fontSize: 18px
    fontWeight: '400'
    lineHeight: '1.6'
    letterSpacing: '0'
  body-md:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: '1.5'
    letterSpacing: '0'
  body-sm:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '400'
    lineHeight: '1.5'
    letterSpacing: '0'
  label-caps:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '700'
    lineHeight: '1.2'
    letterSpacing: 0.05em
  data-tabular:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '500'
    lineHeight: '1.4'
    letterSpacing: '0'
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  base: 4px
  xs: 0.5rem
  sm: 1rem
  md: 1.5rem
  lg: 2.5rem
  xl: 4rem
  gutter: 1.5rem
  sidebar-width: 280px
---

## Brand & Style

The design system is engineered to balance clinical authority with empathetic accessibility. Targeting mental health professionals and administrators, the visual language avoids the sterility of traditional medical software in favor of a "Warm Corporate" aesthetic. 

The style is a hybrid of **Minimalism** and **Modern Corporate**. It prioritizes high legibility and intentional whitespace to reduce cognitive load for users dealing with sensitive patient data. The interface must feel secure and stable; this is achieved through a grounded color palette and a structured grid that conveys a sense of organized calm.

## Colors

The palette is anchored by **Deep Navy**, used for primary navigation and core branding to establish trust and professional weight. **Soft Teal** serves as the functional accent, highlighting active states, primary actions, and positive health indicators without being visually jarring.

The **Warm White** background provides a softer alternative to pure white, reducing eye strain during long sessions of clinical documentation. A tertiary "Teal Tint" (#E8F4F2) is utilized for subtle highlights, such as row hover states or secondary badge backgrounds, maintaining a cohesive thread of the brand accent throughout the interface.

## Typography

This design system utilizes **Inter** for its exceptional legibility and systematic feel. The type hierarchy is strictly defined to ensure that clinical notes and patient records are easily scannable. 

- **Headlines:** Use Semi-Bold weights in Deep Navy to provide clear section anchors.
- **Body Text:** Set in a slightly dark grey (#334155) rather than pure black to maintain the "approachability" factor.
- **Labels:** Use uppercase with increased letter spacing for small metadata and table headers to distinguish them from editable data.
- **Data Display:** For tabular information, a medium weight (500) is used to ensure clarity against white backgrounds.

## Layout & Spacing

The layout follows a **Fluid Grid** model with a fixed sidebar for primary navigation. 

- **Sidebar:** Positioned on the left, using a Deep Navy background to frame the content. It remains collapsed on smaller viewports but stays persistent on desktop to allow quick switching between patient profiles and scheduling.
- **Main Content:** Utilizes a max-width container of 1440px for dashboard views to prevent data-rich tables from becoming over-extended.
- **Rhythm:** An 8px/4px base unit system is applied. Cards and form groupings use 24px (md) padding to maintain an airy, un-cramped feeling even when displaying dense information.

## Elevation & Depth

Hierarchy is established through **Tonal Layers** and **Ambient Shadows**. 

The background layer is Warm White. The primary surface layer (Cards, Modals) is Pure White. Depth is created using a single, consistent shadow style:
- **Professional Shadow:** A very soft, diffused shadow with a Y-offset of 4px and a 12px blur, utilizing the Navy color at a very low opacity (4%) to keep the elevation grounded and integrated.

Large modals use a backdrop blur (12px) to focus the counsellor’s attention on the task at hand, creating a digital "private room" for data entry.

## Shapes

The shape language is defined by the **12px (0.75rem)** radius. This specific value is chosen to strike a balance between the "friendly" circles of consumer apps and the "rigid" squares of legacy enterprise software.

- **Primary Elements:** Buttons, Input Fields, and Cards all share the 12px radius.
- **Small Elements:** Checkboxes and small tags use a 4px radius to maintain sharp definition.
- **Interactive States:** Focus states are indicated by a 2px Deep Navy ring with a 2px offset, ensuring accessibility and a sense of "secure selection."

## Components

### Tables & Data
Professional tables are the core of the portal. They use a flat style with 1px Soft Grey borders (#E2E8F0) and no vertical lines. The header row is slightly tinted in Deep Navy (at 5% opacity) with labels in uppercase Inter.

### Data-Rich Cards
Cards act as containers for patient snapshots. They use a white surface, a 1px border (#F1F5F9), and the "Professional Shadow" on hover. Information inside cards should be grouped using Soft Teal icons for quick visual categorization.

### Secure Form Elements
Inputs must feel robust. They use a white background, a 1px #CBD5E1 border, and 16px of horizontal padding. On focus, the border transitions to Soft Teal with a subtle glow. Error states are clearly marked with an orange-red border and a supporting icon.

### Sidebar Navigation
The sidebar uses Deep Navy as its base. Active links are indicated by a Soft Teal vertical "pill" on the left edge and a subtle background highlight. Icons should be "Duotone" style, using Navy and Teal for a sophisticated, branded look.

### Additional Components
- **Status Pills:** High-contrast capsules for "Active," "Pending," or "Archived" statuses, using the Teal and Neutral palettes.
- **Progress Bars:** Thin, Soft Teal bars used in patient treatment plans to show journey completion.