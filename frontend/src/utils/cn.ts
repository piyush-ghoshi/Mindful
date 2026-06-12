/** Tiny classname joiner — avoids installing clsx */
export function cn(...classes: (string | undefined | false | null)[]): string {
  return classes.filter(Boolean).join(' ');
}
