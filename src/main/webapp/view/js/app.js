/* ==========================================================================
   app.js — JAVASCRIPT DE LA PÁGINA PÚBLICA (index.html)

   Este archivo controla la página principal del portal de noticias.
   Se encarga de:
     1. Cargar y mostrar todas las noticias en la cuadrícula.
     2. Gestionar el modal de login (abrir/cerrar).
     3. Gestionar el inicio de sesión del administrador.
     4. Adaptar la interfaz según si hay admin logueado o no.
   ========================================================================== */


/* ==========================================================================
   EVENTOS (LISTENERS)

   'DOMContentLoaded' se dispara cuando el navegador terminó de leer el HTML
   y construyó el DOM. Es el momento correcto para añadir listeners y
   manipular elementos, porque antes podrían no existir todavía.
   ========================================================================== */
document.addEventListener('DOMContentLoaded', function() {

    // Primero cargamos las noticias. Cuando terminen, comprobamos la sesión
    // para mostrar u ocultar los controles de admin en el header.
    // Usamos .then() para asegurarnos de que comprobarSesion() se ejecute
    // DESPUÉS de que las noticias ya estén en el DOM.
    cargarNoticias().then(function() {
        comprobarSesion();
    });

    // Al hacer clic en "Iniciar sesión" → abrimos el modal de login.
    document.getElementById('btn-abrir-login').onclick = function() {
        abrirModal('modal-login');
    };

    // Al hacer clic en el botón de cerrar → cerramos el modal.
    document.getElementById('btn-cerrar-login').onclick = function() {
        cerrarModal('modal-login');
    };

    // Al enviar el formulario de login → llamamos a iniciarSesion().
    document.getElementById('form-login').onsubmit = iniciarSesion;
});


/* ==========================================================================
   PETICIONES AL BACKEND (SERVLETS JAVA) Y PINTADO DEL DOM
   ========================================================================== */

/* --------------------------------------------------------------------------
   FUNCIÓN: cargarNoticias

   Hace una petición HTTP GET al backend para obtener todas las noticias
   y las muestra en la página como tarjetas HTML.

   Devuelve la Promise de fetch() para poder encadenar .then() desde fuera.
   -------------------------------------------------------------------------- */
function cargarNoticias() {
    // fetch() devuelve una Promise que se resuelve con el objeto Response HTTP.
    // La URL "../api/noticias/listar" apunta al Servlet ListarNoticiasServlet.
    // ".." sube un nivel desde /view/ hasta la raíz de la aplicación.
    return fetch('../api/noticias/listar')
        // PRIMER .then(): recibe el objeto Response y parsea el JSON.
        // res.json() también devuelve una Promise, por eso la retornamos (return).
        // Si no hacemos return aquí, el siguiente .then() recibiría undefined.
        .then(function(res) {
            return res.json();
        })
        // SEGUNDO .then(): recibe el array de noticias ya parseado como objetos JS.
        // Aquí construimos el HTML de las tarjetas y lo insertamos en el DOM.
        .then(function(noticias) {
            // Buscamos el contenedor de la cuadrícula de noticias en el HTML.
            var grid = document.getElementById('grid-todas');

            // Si el elemento no existe (prevención de errores), salimos.
            if (!grid) return;

            // Vaciamos el contenedor antes de insertar para evitar duplicados.
            grid.innerHTML = '';

            // forEach() recorre cada noticia del array.
            // "n" es cada objeto: {id, titulo, contenido, autor, nombreImagen, ...}
            noticias.forEach(function(n) {
                // Creamos un elemento <article> en memoria (no está en la página aún).
                var art = document.createElement('article');

                // Asignamos la clase CSS para dar estilo de tarjeta.
                art.className = 'tarjeta-noticia';

                // Si la noticia tiene imagen → usamos su URL. Si no → placeholder.
                // El operador && comprueba las tres condiciones.
                var imgSrc = (n.nombreImagen && n.nombreImagen !== 'null' && n.nombreImagen !== '')
                    ? '../uploads/' + n.nombreImagen
                    : 'https://placehold.co/600x400/1a1a2e/e0e0e0?text=Nexo+Digital';

                // Escribimos el HTML interno de la tarjeta con la imagen y el título.
                art.innerHTML =
                    '<img src="' + imgSrc + '" class="imagen-placeholder" alt="' + n.titulo + '">' +
                    '<div class="contenido-tarjeta"><h3>' + n.titulo + '</h3></div>';

                // Al hacer clic en la tarjeta, navegamos a la página de la noticia completa.
                // Guardamos n.id en una variable local para evitar problemas de closure en el bucle.
                (function(id) {
                    art.onclick = function() {
                        window.location.href = 'noticia.html?id=' + id;
                    };
                })(n.id);

                // Insertamos la tarjeta en el contenedor de la cuadrícula.
                grid.appendChild(art);
            });
        })
        // .catch(): captura cualquier error de red o de parseo de JSON.
        // Se ejecuta si fetch() falla (sin conexión, servidor caído)
        // o si res.json() falla (respuesta no es JSON válido).
        .catch(function(e) {
            console.error('Error al cargar noticias:', e);
        });
}

/* --------------------------------------------------------------------------
   FUNCIONES DE MODAL

   Un modal es una ventana emergente que se controla con CSS:
   display 'flex' lo muestra, 'none' lo oculta.
   -------------------------------------------------------------------------- */
function abrirModal(id) { document.getElementById(id).style.display = 'flex'; }
function cerrarModal(id) { document.getElementById(id).style.display = 'none'; }


/* ==========================================================================
   SISTEMA DE SESIONES Y LOGIN
   ========================================================================== */

/* --------------------------------------------------------------------------
   FUNCIÓN: iniciarSesion

   Se llama al enviar el formulario de login.
   Envía usuario y contraseña al servidor con POST y gestiona la respuesta.
   -------------------------------------------------------------------------- */
function iniciarSesion(e) {
    // Cancelamos el comportamiento por defecto del formulario (recargar la página).
    // Queremos manejar el envío mediante JavaScript (AJAX).
    e.preventDefault();

    // Leemos los valores escritos por el usuario en los campos del formulario.
    var usuario = document.getElementById('usuario').value;
    var password = document.getElementById('password').value;

    // Enviamos la petición POST al Servlet de Login.
    // "method: 'POST'" → es una petición POST, no GET.
    // "headers" → indicamos el formato del body: clave=valor&clave=valor.
    // "body" → los datos enviados al servidor.
    // encodeURIComponent() convierte caracteres especiales para ir seguros en la URL.
    fetch('../api/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: 'usuario=' + encodeURIComponent(usuario) + '&password=' + encodeURIComponent(password)
    })
        // .then(): recibe el objeto Response cuando el servidor responde.
        // res.ok es true si el código HTTP es 200-299 (éxito).
        // El LoginServlet devuelve 200 si el login es correcto, 401 si no lo es.
        .then(function(res) {
            if (res.ok) {
                // Login correcto → redirigimos al panel de administración.
                window.location.href = 'admin.html';
            } else {
                // Login incorrecto → avisamos al usuario.
                alert('Usuario o contraseña incorrectos');
            }
        })
        // .catch(): solo se ejecuta si hay error de RED (sin internet, servidor caído).
        // Un error 401 del servidor NO entra aquí, entra en el .then() anterior.
        .catch(function(err) {
            console.error('Error al iniciar sesión:', err);
            alert('Error de conexión con el servidor');
        });
}

/* --------------------------------------------------------------------------
   FUNCIÓN: comprobarSesion

   Pregunta al servidor si hay una sesión de admin activa.
   Ajusta la interfaz (botones de la barra de navegación) según el resultado.
   -------------------------------------------------------------------------- */
function comprobarSesion() {
    // GET /api/sesion → SesionServlet devuelve {"logueado": true/false, "usuario": "..."}
    fetch('../api/sesion')
        // Primer .then(): parseamos el JSON de la respuesta.
        .then(function(res) {
            return res.json();
        })
        // Segundo .then(): recibimos el objeto {logueado, usuario} y actualizamos la UI.
        .then(function(data) {
            actualizarUI(data.logueado);
        })
        // .catch(): si hay error de red o JSON inválido, asumimos que no está logueado.
        .catch(function(err) {
            console.error('Error al comprobar sesión:', err);
            actualizarUI(false);
        });
}

/* --------------------------------------------------------------------------
   FUNCIÓN: actualizarUI

   Muestra u oculta los controles de la barra de navegación según si hay
   un administrador logueado o no.
   -------------------------------------------------------------------------- */
function actualizarUI(logueado) {
    // Referencia a los dos grupos de controles del header.
    var controlesAdmin    = document.getElementById('controles-admin');    // "Panel Admin" / "Cerrar sesión"
    var controlesPublicos = document.getElementById('controles-publicos'); // "Iniciar sesión"

    if (logueado) {
        // Admin logueado → mostramos sus controles y ocultamos los públicos.
        controlesAdmin.style.display    = 'block';
        controlesPublicos.style.display = 'none';
    } else {
        // Sin sesión → mostramos los controles públicos y ocultamos los de admin.
        controlesAdmin.style.display    = 'none';
        controlesPublicos.style.display = 'block';
    }
}

