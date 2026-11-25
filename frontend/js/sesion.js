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
    document.body.dataset.sinPermiso = "true";
  }
  return sesion;
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
