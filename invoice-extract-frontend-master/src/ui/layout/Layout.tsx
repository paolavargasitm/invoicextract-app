import { Outlet } from "react-router-dom";
import Navbar from "./Navbar";
import Footer from "./Footer";

export default function Layout() {
  return (
    <div style={{ background: "var(--bg)", minHeight: "100vh", display: 'flex', flexDirection: 'column' }}>
      <Navbar />
      <main style={{ maxWidth: 1600, margin: "0 auto", padding: "24px 16px", flex: 1, width: '100%' }}>
        <Outlet />
      </main>
      <Footer />
    </div>
  );
}
