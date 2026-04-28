import React from "react";

type Variant = "primary" | "success" | "secondary" | "ghost";

interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: Variant;
}

const base =
  "px-5 py-2.5 rounded-lg font-semibold transition-all duration-200 active:scale-95";

const styles: Record<Variant, string> = {
  primary:
    "bg-blue-600 text-white shadow-md hover:bg-blue-700 hover:shadow-lg",
  success:
    "bg-green-600 text-white shadow-md hover:bg-green-700 hover:shadow-lg",
  secondary:
    "bg-white border border-gray-300 text-gray-700 hover:bg-gray-100",
  ghost:
    "bg-blue-50 text-blue-700 hover:bg-blue-100 hover:text-blue-800 px-3 py-1.5",
};

const Button: React.FC<ButtonProps> = ({
  variant = "primary",
  className = "",
  ...props
}) => {
  return (
    <button
      className={`${base} ${styles[variant]} ${className}`}
      {...props}
    />
  );
};

export default Button;