import React from "react";
import { createRoot } from "react-dom/client";
import App from "./App";
import { initKeycloak } from "./keycloak";

async function bootstrap() {
  await initKeycloak();
  const root = createRoot(document.getElementById("root"));
  root.render(<App />);
}
bootstrap();
