---
name: Serenity & Support
colors:
  surface: '#f9f9ff'
  surface-dim: '#cbdaff'
  surface-bright: '#f9f9ff'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f1f3ff'
  surface-container: '#e8edff'
  surface-container-high: '#e0e8ff'
  surface-container-highest: '#d8e2ff'
  on-surface: '#081b39'
  on-surface-variant: '#3d4946'
  inverse-surface: '#1f304f'
  inverse-on-surface: '#edf0ff'
  outline: '#6c7a76'
  outline-variant: '#bcc9c5'
  surface-tint: '#006b5f'
  primary: '#006b5f'
  on-primary: '#ffffff'
  primary-container: '#3bbfad'
  on-primary-container: '#004941'
  inverse-primary: '#5cdac7'
  secondary: '#546160'
  on-secondary: '#ffffff'
  secondary-container: '#d7e5e3'
  on-secondary-container: '#5a6766'
  tertiary: '#b91c24'
  on-tertiary: '#ffffff'
  tertiary-container: '#ff8a82'
  on-tertiary-container: '#860010'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#7bf7e3'
  primary-fixed-dim: '#5cdac7'
  on-primary-fixed: '#00201c'
  on-primary-fixed-variant: '#005047'
  secondary-fixed: '#d7e5e3'
  secondary-fixed-dim: '#bbc9c7'
  on-secondary-fixed: '#111e1d'
  on-secondary-fixed-variant: '#3c4948'
  tertiary-fixed: '#ffdad7'
  tertiary-fixed-dim: '#ffb3ad'
  on-tertiary-fixed: '#410004'
  on-tertiary-fixed-variant: '#930013'
  background: '#f9f9ff'
  on-background: '#081b39'
  surface-variant: '#d8e2ff'
typography:
  headline-xl:
    fontFamily: Inter
    fontSize: 48px
    fontWeight: '700'
    lineHeight: '1.2'
  headline-lg:
    fontFamily: Inter
    fontSize: 32px
    fontWeight: '600'
    lineHeight: '1.3'
  headline-md:
    fontFamily: Inter
    fontSize: 24px
    fontWeight: '600'
    lineHeight: '1.4'
  body-lg:
    fontFamily: Inter
    fontSize: 18px
    fontWeight: '400'
    lineHeight: '1.6'
  body-md:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: '1.5'
  body-sm:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '400'
    lineHeight: '1.5'
  label-md:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '600'
    lineHeight: '1.2'
    letterSpacing: 0.02em
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  max-width: 1440px
  base-unit: 8px
  container-padding: 40px
  gutter: 24px
  margin-sm: 16px
  margin-md: 32px
  margin-lg: 64px
---

## Brand & Style

The brand personality of the design system is rooted in empathy, safety, and professional reliability. Designed for students navigating mental health challenges, the interface prioritizes a sense of calm and accessibility over high-energy visuals. The target audience requires a user interface that feels like a supportive environment rather than a clinical tool.

The chosen style is **Minimalism** with a **Modern Corporate** influence. It utilizes generous whitespace, a restricted but intentional color palette, and subtle depth to guide the user's focus toward content and resources without overwhelming the senses. The aesthetic is clean and breathable, fostering a serene atmosphere that reduces cognitive load and anxiety.

## Colors

The color palette is anchored by a soft teal primary color that symbolizes growth and healing. This is balanced against a warm white background to prevent the "starkness" of pure white, creating a more inviting and less institutional feel. 

- **Primary (#3BBFAD):** Used for primary actions, active states, and brand-building elements.
- **Secondary (#E6F4F2):** A light teal used for subtle background containers, card headers, or hover states to soften the interface.
- **Text & Neutral (#1A2B4A):** A deep navy serves as the primary text color to ensure high legibility and a grounded, professional feel.
- **Border (#E2E8F0):** A soft grey for structural definition that remains unobtrusive.
- **Alert/SOS (#E53E3E):** A specific red reserved exclusively for the floating SOS button to ensure it is immediately recognizable in emergencies.

## Typography

The design system utilizes **Inter** for all typographic needs. This choice provides a systematic, highly readable, and professional foundation that excels in both large headlines and dense informational text. 

Hierarchy is established through weight and color rather than excessive size differences. Headlines are set in the deep navy text color to provide a strong anchor for each page. Body text maintains a generous line height to ensure readability for students who may be experiencing stress or fatigue. Labels and smaller UI elements use slightly increased letter spacing and semi-bold weights to maintain clarity at smaller scales.

## Layout & Spacing

This design system follows a **Fixed Grid** model centered on a 1440px canvas. This ensures a consistent reading experience and prevents content from becoming overly stretched on ultrawide monitors, which can be disorienting.

The layout uses a 12-column system with 24px gutters. Spacing follows an 8px base rhythm to ensure mathematical harmony between elements. Content is grouped into logical modules with ample "breathing room" (64px+ vertical margins) between major sections to maintain the serene, supportive vibe and prevent the user from feeling crowded.

## Elevation & Depth

To maintain the professional and soft aesthetic, elevation is communicated through **Ambient Shadows** and **Tonal Layers**. Instead of harsh shadows, this system uses highly diffused, low-opacity shadows with a subtle navy tint to match the typography.

- **Level 1 (Cards/Inputs):** A very soft, barely-there shadow to lift elements slightly off the warm white background.
- **Level 2 (Hover/Active):** An increased blur radius and slightly more opacity to indicate interactivity.
- **Tonal Depth:** Background light teal surfaces (#E6F4F2) are used to create "wells" or zones of content within a page without requiring elevation, keeping the interface feeling flat and modern.

## Shapes

The shape language is defined by a consistent **12px (0.75rem)** corner radius for all primary UI containers and cards. This specific roundedness is key to the "supportive" feel, as it avoids the aggression of sharp corners while remaining more professional and structured than full pill-shaped components.

- **Primary Radius:** 12px for cards, input fields, and large buttons.
- **Small Radius:** 6px for smaller elements like checkboxes or nested tags.
- **Circular:** Reserved exclusively for user avatars and the floating SOS action button to make it stand out against the geometric grid.

## Components

### Navigation
The top navigation bar is fixed, featuring a clear logo on the far left and simple, text-based navigation links on the right. It uses a soft bottom border or a very low-elevation shadow to separate it from the main content.

### Buttons
- **Primary:** Solid #3BBFAD with white text, 12px rounded corners.
- **Secondary:** Outlined with a 1px soft grey border or light teal background with primary teal text.
- **SOS Button:** A persistent, circular floating action button (FAB) in the bottom-right corner. It is colored in the alert red (#E53E3E) with a prominent white icon or "SOS" label, ensuring it is reachable at all times.

### Cards
Cards are the primary content vessel. They feature the 12px roundedness, a white background, and a soft ambient shadow. For grouped content, cards may use the light teal secondary color as a header background.

### Inputs & Form Elements
Input fields use a 1px soft grey border that transitions to the primary teal on focus. Labels are always positioned above the field for maximum accessibility.

### Interactive States
Hover states should be subtle, typically a slight darkening of the teal primary or a soft elevation lift for cards. Transitions should be smooth (200-300ms) to maintain the serene atmosphere.