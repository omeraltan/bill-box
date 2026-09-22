type LogoMarkProps = {
  className?: string;
};

export function LogoMark({ className }: LogoMarkProps) {
  return <img className={className} src="/favicon.svg" alt="" width={32} height={32} />;
}
