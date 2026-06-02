// Canonical size options for variants, used by the admin variant dropdown.
// "Adjustable" is the one-size value — it replaces the old ONE_SIZE / OS sentinels.
export const VARIANT_SIZES = ['S', 'M', 'L', 'XL', 'XXL', 'Adjustable'];

const ONE_SIZE_VALUES = ['Adjustable', 'ONE_SIZE', 'OS'];

// True when a size shouldn't be shown as a distinct size label (one-size items,
// or legacy/null sizes on pre-variant orders).
export function isOneSize(size) {
  return !size || ONE_SIZE_VALUES.includes(size);
}
