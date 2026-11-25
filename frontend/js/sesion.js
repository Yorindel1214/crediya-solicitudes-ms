// HU3: manejo de la sesión en el navegador.
// El inicio de sesión lo hace el microservicio de AUTENTICACIÓN (puerto 8080); este frontend
// guarda el token (JWT) y lo envía en la cabecera Authorization al microservicio de Solicitudes.
const API_AUTENTICACION = "http://localhost:8080";
const CLAVE_SESION = "crediya.sesion";

function guardarSesion(datos) {
  localStorage.setItem(CLAVE_SESION, JSON.stringify({
    token: datos.token,
    correo: datos.correo_electronico,
    rol: datos.rol,
    expira: Date.now() + datos.expira_en * 1000,
  }));
}

function obtenerSesion() {
  try {
    const sesion = JSON.parse(localStorage.getItem(CLAVE_SESION));
    if (!sesion || Date.now() > sesion.expira) return null;
    return sesion;
  } catch (e) {
    return null;
  }
}

// Lee los datos que vienen dentro del token (documento, nombre, rol). Solo para mostrarlos:
// quien realmente valida el token es el backend.
function datosDelToken() {
  const sesion = obtenerSesion();
  if (!sesion) return {};
  try {
    const parte = sesion.token.split(".")[1].replace(/-/g, "+").replace(/_/g, "/");
    const json = decodeURIComponent(atob(parte).split("").map((c) => "%" + ("00" + c.charCodeAt(0).toString(16)).slice(-2)).join(""));
    return JSON.parse(json);
  } catch (e) {
    return {};
  }
}

function cerrarSesion() {
  localStorage.removeItem(CLAVE_SESION);
  window.location.href = "login.html";
}

function cabecerasConToken() {
  const sesion = obtenerSesion();
  return {
    "Content-Type": "application/json",
    ...(sesion ? { Authorization: `Bearer ${sesion.token}` } : {}),
  };
}

// Si no hay sesión se envía al login; si el rol no sirve para la página se marca el body
function requerirSesion(rolesPermitidos) {
  const sesion = obtenerSesion();
  if (!sesion) {
    window.location.href = `login.html?volver=${encodeURIComponent(location.pathname.split("/").pop())}`;
    return null;
  }
  if (rolesPermitidos && !rolesPermitidos.includes(sesion.rol)) {
    bloquearPagina(sesion.rol, rolesPermitidos);
    return null;
  }
  return sesion;
}

// A dónde se sugiere ir según el rol cuando la página no le corresponde
const DESTINO_POR_ROL = {
  CLIENTE: { texto: "Ir a solicitar un préstamo", url: "solicitar.html" },
  ADMIN: { texto: "Ir a registrar usuarios", url: "http://localhost:8000/login.html" },
};

// Oculta el contenido de la página y muestra un aviso de permisos.
// Es solo para la experiencia de usuario: la seguridad real la aplica la API (responde 403).
function bloquearPagina(rol, rolesPermitidos) {
  const main = document.querySelector("main");
  if (!main) return;
  for (const hijo of main.children) hijo.hidden = true;

  const panel = document.createElement("section");
  panel.className = "panel acceso-denegado";
  const titulo = document.createElement("h1");
  titulo.textContent = "No tienes permiso para esta página";
  const texto = document.createElement("p");
  texto.textContent = `Iniciaste sesión con rol ${rol}. Esta página es solo para: ${rolesPermitidos.join(" o ")}.`;
  const acciones = document.createElement("div");
  acciones.className = "acceso-denegado__acciones";
  const destino = DESTINO_POR_ROL[rol];
  if (destino) {
    const enlace = document.createElement("a");
    enlace.className = "boton";
    enlace.href = destino.url;
    enlace.textContent = destino.texto;
    acciones.appendChild(enlace);
  }
  const salir = document.createElement("button");
  salir.type = "button";
  salir.className = "boton boton--fantasma";
  salir.textContent = "Cerrar sesión y entrar con otro usuario";
  salir.addEventListener("click", cerrarSesion);
  acciones.appendChild(salir);
  panel.append(titulo, texto, acciones);
  main.prepend(panel);
}

function pintarSesion() {
  const caja = document.getElementById("usuarioSesion");
  if (!caja) return;
  const sesion = obtenerSesion();
  caja.innerHTML = "";
  if (!sesion) {
    const enlace = document.createElement("a");
    enlace.href = "login.html";
    enlace.textContent = "Iniciar sesión";
    caja.appendChild(enlace);
    return;
  }
  const texto = document.createElement("span");
  texto.textContent = `${sesion.correo} · ${sesion.rol}`;
  const salir = document.createElement("button");
  salir.type = "button";
  salir.className = "boton boton--fantasma boton--mini";
  salir.textContent = "Salir";
  salir.addEventListener("click", cerrarSesion);
  caja.append(texto, salir);
}

pintarSesion();
