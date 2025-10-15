import { BrowserRouter, Routes, Route } from "react-router-dom";
import LoginPage from "./ui/login/pages/LoginPage";
import RegisterPage from "./ui/register/pages/RegisterPage";
import EmailConfigPage from "./ui/email-config/pages/EmailConfigPage";
import ProfilePage from "./ui/profile/pages/ProfilePage";
import UserManagementPage from "./ui/users-management/pages/UserManagementPage";
import InvoiceDetailPage from "./ui/invoices/pages/InvoiceDetailPage";
import Layout from "./ui/layout/Layout";
import ProtectedRoute from "./auth/ProtectedRoute";
import MappingPage from "./ui/mappings/pages/MappingPage";
import ErpConfigPage from "./ui/erp/pages/ErpConfigPage";
import DashboardPage from "./ui/dashboard/pages/DashboardPage";
import Home from "./ui/home/pages/HomePage";

export default function AppRouter() {
    return (
        <BrowserRouter>
            <Routes>
                <Route element={<Layout />}>
                    <Route index element={<Home />} />
                    <Route path="dashboard" element={<ProtectedRoute roles={["ADMIN","FINANZAS"]}><DashboardPage /></ProtectedRoute>} />
                    {/* Login/Register remain accessible but typical flow is Keycloak redirect */}
                    <Route path="login" element={<LoginPage />} />
                    <Route path="register" element={<RegisterPage />} />
                    <Route path="profile" element={<ProtectedRoute><ProfilePage /></ProtectedRoute>} />
                    <Route path="users-management" element={<ProtectedRoute roles={["ADMIN"]}><UserManagementPage /></ProtectedRoute>} />
                    <Route path="email-config" element={<ProtectedRoute roles={["ADMIN"]}><EmailConfigPage /></ProtectedRoute>} />
                    <Route path="invoices/:id" element={<ProtectedRoute roles={["ADMIN","FINANZAS"]}><InvoiceDetailPage /></ProtectedRoute>} />
                    <Route path="invoices" element={<ProtectedRoute roles={["ADMIN","FINANZAS"]}><DashboardPage /></ProtectedRoute>} />
                    <Route path="mapping" element={<ProtectedRoute roles={["ADMIN","TECNICO"]}><MappingPage /></ProtectedRoute>} />
                    <Route path="erp-config" element={<ProtectedRoute roles={["ADMIN","TECNICO"]}><ErpConfigPage /></ProtectedRoute>} />
                </Route>
            </Routes>
        </BrowserRouter>
    );
}
