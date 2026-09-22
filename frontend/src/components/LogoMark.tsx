type LogoMarkProps = {
  className?: string;
};

export function LogoMark({ className }: LogoMarkProps) {
  const classes = className ? `logo-mark ${className}` : "logo-mark";
  return (
    <span className={classes}>
      <img src="/favicon.svg" alt="" width={32} height={32} />
    </span>
  );
}
