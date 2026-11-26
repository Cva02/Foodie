
import { Navigate, Outlet, useLocation } from "react-router-dom";
import { useEffect, useState } from "react";
import { getSellerSession } from "../api";

export default function SellerProtected() {
  const location = useLocation();
  const [session, setSession] = useState(getSellerSession);

  useEffect(() => {
    const handleUnauthorized = () => {
      setSession(null);
    };

    window.addEventListener(
      "foodie-seller-unauthorized",
      handleUnauthorized
    );

    return () => {
      window.removeEventListener(
        "foodie-seller-unauthorized",
        handleUnauthorized
      );
    };
  }, []);

  if (!session?.token) {
    return (
      <Navigate
        to="/seller/login"
        replace
        state={{ from: location.pathname }}
      />
    );
  }

  return <Outlet />;
}
