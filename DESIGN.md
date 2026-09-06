---
name: Verdant Tech
colors:
  surface: '#131313'
  surface-dim: '#131313'
  surface-bright: '#393939'
  surface-container-lowest: '#0e0e0e'
  surface-container-low: '#1c1b1b'
  surface-container: '#201f1f'
  surface-container-high: '#2a2a2a'
  surface-container-highest: '#353534'
  on-surface: '#e5e2e1'
  on-surface-variant: '#c0c9bb'
  inverse-surface: '#e5e2e1'
  inverse-on-surface: '#313030'
  outline: '#8a9386'
  outline-variant: '#41493e'
  surface-tint: '#91d78a'
  primary: '#91d78a'
  on-primary: '#003909'
  primary-container: '#1b5e20'
  on-primary-container: '#90d689'
  inverse-primary: '#2a6b2c'
  secondary: '#91d78a'
  on-secondary: '#003909'
  secondary-container: '#0f5518'
  on-secondary-container: '#84c97d'
  tertiary: '#a2d3a4'
  on-tertiary: '#0a3817'
  tertiary-container: '#2f5b36'
  on-tertiary-container: '#a0d1a2'
  error: '#ffb4ab'
  on-error: '#690005'
  error-container: '#93000a'
  on-error-container: '#ffdad6'
  primary-fixed: '#acf4a4'
  primary-fixed-dim: '#91d78a'
  on-primary-fixed: '#002203'
  on-primary-fixed-variant: '#0c5216'
  secondary-fixed: '#acf4a4'
  secondary-fixed-dim: '#91d78a'
  on-secondary-fixed: '#002203'
  on-secondary-fixed-variant: '#0c5216'
  tertiary-fixed: '#bdefbe'
  tertiary-fixed-dim: '#a2d3a4'
  on-tertiary-fixed: '#002109'
  on-tertiary-fixed-variant: '#24502c'
  background: '#131313'
  on-background: '#e5e2e1'
  surface-variant: '#353534'
typography:
  headline-lg:
    fontFamily: Inter
    fontSize: 40px
    fontWeight: '700'
    lineHeight: 48px
    letterSpacing: -0.02em
  headline-md:
    fontFamily: Inter
    fontSize: 28px
    fontWeight: '600'
    lineHeight: 36px
    letterSpacing: -0.01em
  headline-sm:
    fontFamily: Inter
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
  title-lg:
    fontFamily: Inter
    fontSize: 22px
    fontWeight: '500'
    lineHeight: 28px
  body-lg:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
    letterSpacing: 0.01em
  body-md:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  label-lg:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '600'
    lineHeight: 16px
    letterSpacing: 0.05em
  headline-lg-mobile:
    fontFamily: Inter
    fontSize: 32px
    fontWeight: '700'
    lineHeight: 40px
rounded:
  sm: 0.5rem
  DEFAULT: 1rem
  md: 1.5rem
  lg: 2rem
  xl: 3rem
  full: 9999px
spacing:
  base: 4px
  xs: 4px
  sm: 8px
  md: 16px
  lg: 24px
  xl: 32px
  gutter: 16px
  margin-mobile: 16px
  margin-desktop: 48px
---

## Brand & Style
The design system for this personal agriculture journal bridges the gap between raw botanical data and high-tech precision. The personality is grounded, observant, and deeply technical—evoking the feeling of a professional laboratory situated within a thriving greenhouse.

The aesthetic follows a **Material 3 Dark** foundation, modified with a "Verdant Tech" overlay. It utilizes deep, immersive backgrounds to reduce eye strain during late-night crop monitoring, while employing vibrant green accents to highlight growth and vitality. The interface is "local-first," emphasizing speed, privacy, and offline reliability through a systematic and utilitarian visual language.

## Colors
The palette is rooted in the "Verdant Tech" philosophy, using high-contrast greens against a near-black foundation to ensure maximum legibility and a focused atmosphere.

- **Primary (Forest Green):** Used for key actions and structural branding elements. It represents the strength and stability of established growth.
- **Secondary (Sage):** The primary accent for interactive states, toggles, and positive growth indicators. Its high luminosity ensures visibility against the dark background.
- **Neutral (Deep Obsidian):** The #131313 base provides a non-distracting canvas that allows the biological data to take center stage.
- **Surface Tiers:** Use subtle shifts in lightness (Surface Variant) to define container boundaries rather than heavy borders.

## Typography
This design system relies exclusively on **Inter** to maintain a systematic, "tech-first" feel. High contrast is achieved through aggressive weight scaling and tight letter-spacing on larger headlines.

All headings should use a semi-bold or bold weight to anchor the page. Body text remains clean and highly legible with a slightly increased line-height to accommodate scientific nomenclature and long-form gardening notes. Labels and data points should utilize the "label-lg" style with slight tracking (letter-spacing) to mimic technical instrument readouts.

## Layout & Spacing
The layout follows a strict 4px soft grid. In accordance with Material 3 principles, the system uses a fluid grid for mobile and a centered, max-width container for desktop displays (max 1200px).

- **Mobile:** 4-column grid with 16px margins.
- **Tablet:** 8-column grid with 24px margins.
- **Desktop:** 12-column grid with 24px gutters and adaptive margins.

Information density should be high for data-heavy journaling views, but generous padding (24px+) should be used for observational "Daily Log" entries to provide a sense of calm.

## Elevation & Depth
Elevation is communicated through **Tonal Layering** rather than traditional drop shadows. In this dark mode environment, higher elevation surfaces are represented by lighter grey-green overlays.

- **Level 0 (Background):** #131313.
- **Level 1 (Cards):** #1C1C1C with a 1px subtle stroke of #2A2A2A.
- **Level 2 (Modals/Pop-ups):** #252525 with a soft, 15% opacity Forest Green glow (0px 8px 24px).

Avoid using pure black shadows; instead, use deep green-tinted shadows to maintain the "Verdant" atmosphere.

## Shapes
The design system employs a distinct contrast in shape language. 

- **Containers & Cards:** Utilize a soft, organic **28px corner radius**. This reflects the rounded, natural shapes of leaves and fruit.
- **Interactive Elements:** Buttons, Chips, and Search Bars are strictly **Pill-shaped** (fully rounded). This reinforces the "tech" aspect of the system, mimicking modern hardware interfaces.
- **Input Fields:** Use the "Soft" (0.5rem) roundedness to differentiate them from action buttons.

## Components
- **Buttons:** All primary buttons are pill-shaped, using the Forest Green background with Sage text for high-contrast visibility.
- **Cards:** The signature component of the design system. Must use a 28px corner radius. They should feature a "Surface Variant" background to pop against the #131313 base.
- **Chips:** Used for plant categories (e.g., "Perennial", "Hydroponic"). These are small pill-shaped elements with a Sage outline and no fill.
- **Input Fields:** Underlined or filled-style with 8px top-corner radius (Material 3 style), utilizing Sage for the active focus state.
- **Growth Progress Bars:** Thick, 8px height bars with pill-shaped caps. The track is the background color, and the progress is a gradient from Forest Green to Sage.
- **Navigation:** A bottom navigation bar (mobile) or side rail (desktop) with pill-shaped active state indicators, adhering to the Material 3 "Navigation Bar" spec.