/* =========================================================================
   FUNCIÓN PRINCIPAL DE ARRANQUE
   ========================================================================= */
/**
 * async: Declara que esta función es asíncrona. Nos permite usar 'await' dentro 
 * de ella para esperar a que terminen procesos lentos (como peticiones a bases de datos) 
 * sin congelar la pestaña del navegador.
 */
async function inicializarPagina() {
    // await hace que la función se pause aquí hasta que cargarNoticias() haya terminado por completo
    await cargarNoticias();
    // Luego comprueba en el backend de Tomcat si la persona que entra tiene el login activo
    comprobarSesion();
}

/* =========================================================================
   EVENTOS (LISTENERS)
   ========================================================================= */
/**
 * document.addEventListener('DOMContentLoaded', ...):
 * Esto es VITAL. Le dice a JavaScript que no haga absolutamente nada hasta que el 
 * navegador haya terminado de leer y dibujar todo el archivo index.html. Si no hacemos esto, 
 * JS intentaría buscar botones que aún no existen y daría errores nulos (null).
 */
document.addEventListener('DOMContentLoaded', () => {
    // Arrancamos el flujo principal
    inicializarPagina();

    // Vinculamos funciones de interfaz a los clics de los botones.
    // 'onclick' es el evento que escucha el clic del ratón.
    // Usamos ()=> (arrow function) para evitar ejecutar la función inmediatamente al leer el código.
    document.getElementById('btn-abrir-login').onclick = () => abrirModal('modal-login');
    document.getElementById('btn-cerrar-login').onclick = () => cerrarModal('modal-login');

    // Interceptamos el evento 'onsubmit' (cuando pulsas 'Entrar' en el formulario de login).
    // Queremos usar nuestra función iniciarSesion en lugar de que el formulario recargue la página.
    document.getElementById('form-login').onsubmit = iniciarSesion;

});

/* =========================================================================
   PETICIONES AL BACKEND (SERVLETS JAVA) Y PINTADO DEL DOM
   ========================================================================= */
/**
 * Esta es la función que hace magia con tu arquitectura MVC. Se comunica con el Controlador (Servlet), 
 * obtiene los Datos del Modelo (DAO -> BBDD) y los pinta en la Vista (HTML).
 */
async function cargarNoticias() {
    try {
        // fetch() hace una petición HTTP GET.
        // OJO A LA RUTA: '../api/noticias/listar'. Como este script se ejecuta dentro de view/js/,
        // tiene que subir un nivel (../) para llegar a la raíz del servidor y luego entrar al servlet.
        const res = await fetch('../api/noticias/listar');

        // Convertimos la respuesta cruda en texto a objetos JSON que JavaScript puede entender fácilmente.
        const noticias = await res.json();

        // Buscamos en el HTML el div contenedor donde deben ir las tarjetas
        const grid = document.getElementById('grid-todas');

        // Medida de seguridad: Si estamos en otra página (ej: admin.html) donde este div no existe, salimos (return).
        if (!grid) return;

        // Vaciamos el div por completo asegurando que no haya basura de cargas anteriores
        grid.innerHTML = '';

        // Recorremos el Array de noticias usando un bucle forEach. La variable 'n' representa cada noticia individual.
        noticias.forEach(n => {
            // document.createElement() crea una etiqueta HTML literal en la memoria del navegador.
            const art = document.createElement('article');
            // Le añadimos la clase que diseñamos en el CSS
            art.className = 'tarjeta-noticia';

            // OPERADOR TERNARIO (? :): Es un if/else en una sola línea.
            // Pregunta: ¿n.nombreImagen existe Y no es el texto 'null' Y no está vacía?
            // Si la respuesta es SÍ (?): La ruta será la carpeta de subidas de tu servidor.
            // Si la respuesta es NO (:): Le ponemos una imagen gris de internet (placeholder) para que no se rompa el diseño.
            const imgSrc = (n.nombreImagen && n.nombreImagen !== 'null' && n.nombreImagen !== '')
                ? `../uploads/${n.nombreImagen}`
                : 'https://placehold.co/600x400/1a1a2e/e0e0e0?text=Nexo+Digital';

            // Usamos Template Literals (las comillas invertidas ``). Nos permiten escribir HTML
            // en múltiples líneas e inyectar variables usando ${variable}.
            // AQUÍ es donde definimos que la tarjeta SOLO tenga la imagen y el título (h3).
            art.innerHTML = `
                <img src="${imgSrc}" class="imagen-placeholder" alt="${n.titulo}">
                <div class="contenido-tarjeta">
                    <h3>${n.titulo}</h3>
                </div>
            `;

            // Al hacer clic redirigimos a la nueva página enviando la ID por URL
            art.onclick = () => window.location.href = 'noticia.html?id=' + n.id;

            // Finalmente, "inyectamos" esta tarjeta desde la memoria del navegador al HTML visible de la web.
            grid.appendChild(art);
        });
    } catch (e) {
        // Si hay un error (ej: servidor caído), lo mostramos en la consola para nosotros los programadores.
        console.error("Error al cargar noticias:", e);
    }
}



/**
 * Funciones reutilizables para manipular el CSS display de los modales (ventanas emergentes)
 */
function abrirModal(id) { document.getElementById(id).style.display = 'flex'; }
function cerrarModal(id) { document.getElementById(id).style.display = 'none'; }

/* =========================================================================
   SISTEMA DE SESIONES Y LOGIN
   ========================================================================= */
/**
 * Se dispara al enviar el formulario del modal.
 * @param {Event} e - El evento 'submit' que nos manda el navegador.
 */
async function iniciarSesion(e) {
    // e.preventDefault() cancela la acción por defecto del HTML.
    // Si no pones esto, al pulsar el botón el formulario recargará la página entera y borrará nuestros procesos.
    e.preventDefault();

    // Capturamos el texto que el usuario ha tecleado en los campos.
    const usuario = document.getElementById('usuario').value;
    const password = document.getElementById('password').value;

    try {
        // Hacemos una petición POST (enviar datos, no pedir datos) a nuestro Servlet de login.
        const res = await fetch('../api/login', {
            method: 'POST',
            // Le decimos al Servlet que le vamos a enviar datos con el formato estándar de un formulario web.
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            // encodeURIComponent "limpia" las variables para que viajen seguras en la URL 
            // (por ejemplo, cambia los espacios por %20 o símbolos raros).
            body: `usuario=${encodeURIComponent(usuario)}&password=${encodeURIComponent(password)}`
        });

        // La propiedad 'ok' de fetch es verdadera si el Servlet devuelve un código 200 a 299 (Éxito).
        if (res.ok) {
            // Login correcto: Forzamos al navegador a redirigirse al archivo admin.html
            window.location.href = 'admin.html';
        } else {
            // Login incorrecto: Servlet devolvió 401 (No Autorizado) o similar.
            alert('Usuario o contraseña incorrectos');
        }
    } catch (err) {
        console.error('Error al iniciar sesión:', err);
        alert('Error de conexión con el servidor');
    }
}

/**
 * Petición silenciosa para preguntar al backend si el usuario que está navegando
 * ya tiene una sesión abierta en Java (HttpSession).
 */
async function comprobarSesion() {
    try {
        // Llamada al Servlet que gestiona la sesión.
        const res = await fetch('../api/sesion');
        const data = await res.json();
        // El servlet nos devolverá algo como { logueado: true } o { logueado: false }.
        // Pasamos ese booleano a nuestra función que actualiza la interfaz visual.
        actualizarUI(data.logueado);
    } catch (err) {
        console.error('Error al comprobar sesión:', err);
        // Ante cualquier error, asumimos que NO está logueado por seguridad.
        actualizarUI(false);
    }
}

/**
 * Modifica qué botones se muestran en el Footer de la página basándose en si el usuario
 * es administrador o no.
 * @param {boolean} logueado - Viene directo de la comprobación en Tomcat.
 */
function actualizarUI(logueado) {
    const controlesAdmin = document.getElementById('controles-admin');
    const controlesPublicos = document.getElementById('controles-publicos');

    if (logueado) {
        // ES ADMINISTRADOR:
        controlesAdmin.style.display = 'block'; // Mostramos el enlace directo "Panel de Admin".
        controlesPublicos.style.display = 'none'; // Ocultamos el botón "Admin" que abre el modal.
    } else {
        // ES UN VISITANTE NORMAL:
        controlesAdmin.style.display = 'none'; // Ocultamos el enlace directo.
        controlesPublicos.style.display = 'block'; // Mostramos el botón que abre el modal para intentar loguearse.
    }
}

/* =========================================================================
   REAJUSTE AUTOMÁTICO AL CAMBIAR DE PESTAÑA (REFLOW BUG FIX)
   ========================================================================= */
document.addEventListener('visibilitychange', () => {
    if (document.visibilityState === 'visible') {
        window.dispatchEvent(new Event('resize'));
    }
});

window.addEventListener('pageshow', () => {
    window.dispatchEvent(new Event('resize'));
});
