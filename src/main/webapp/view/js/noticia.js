/* ==========================================================================
   noticia.js — JAVASCRIPT DE LA PÁGINA DE DETALLE DE NOTICIA (noticia.html)

   Este archivo controla la página que muestra una noticia completa.
   Cuando el usuario hace clic en una tarjeta en la página principal,
   es redirigido a "noticia.html?id=5" (por ejemplo).

   Este script lee el "?id=5" de la URL, pide al servidor los datos
   completos de esa noticia y los muestra en la página.
   ========================================================================== */


/* ==========================================================================
   INICIALIZACIÓN AL CARGAR LA PÁGINA
   ========================================================================== */
document.addEventListener('DOMContentLoaded', function() {
    // Carga los datos de la noticia cuyo ID viene en la URL.
    cargarNoticiaIndividual();
    // Comprueba si hay admin logueado (para mostrar/ocultar controles de admin).
    comprobarSesion();

    // Eventos del modal de login (igual que en app.js).
    document.getElementById('btn-abrir-login').onclick = function() {
        abrirModal('modal-login');
    };
    document.getElementById('btn-cerrar-login').onclick = function() {
        cerrarModal('modal-login');
    };
    document.getElementById('form-login').onsubmit = iniciarSesion;
});


/* ==========================================================================
   CARGA DE LA NOTICIA INDIVIDUAL
   ========================================================================== */

/* --------------------------------------------------------------------------
   FUNCIÓN: cargarNoticiaIndividual

   Lee el parámetro "id" de la URL actual, hace GET a /api/noticias/obtener?id=X
   y muestra el contenido completo de la noticia en el HTML.
   -------------------------------------------------------------------------- */
function cargarNoticiaIndividual() {
    // URLSearchParams analiza los parámetros de la URL de la página actual.
    // window.location.search devuelve la parte después del "?".
    // Ejemplo: si la URL es "noticia.html?id=5" → window.location.search es "?id=5"
    var params  = new URLSearchParams(window.location.search);

    // params.get('id') extrae el valor del parámetro "id".
    // Resultado: "5" (String) o null si no hay parámetro "id" en la URL.
    var idParam = params.get('id');

    // Referencia al elemento del título para poder modificarlo.
    var tituloEl = document.getElementById('noticia-titulo');

    // Si el elemento no existe en el HTML, salimos (prevención de errores).
    if (!tituloEl) return;

    // Si no hay ID en la URL, no sabemos qué noticia cargar.
    if (!idParam) {
        tituloEl.textContent = 'Noticia no encontrada';
        return;
    }

    // Hacemos GET al Servlet ObtenerNoticiaServlet con el ID como parámetro en la URL.
    // Ejemplo de URL resultante: "../api/noticias/obtener?id=5"
    fetch('../api/noticias/obtener?id=' + idParam)
        // Primer .then(): comprobamos el código HTTP y parseamos el JSON.
        .then(function(res) {
            // Si el servidor devuelve un error (404, 500...) → res.ok es false.
            // En ese caso lanzamos un Error para que lo capture el .catch().
            if (!res.ok) {
                throw new Error('HTTP ' + res.status);
            }
            // res.json() devuelve una Promise → la retornamos para encadenar el siguiente .then().
            return res.json();
        })
        // Segundo .then(): recibimos el objeto noticia y rellenamos el HTML.
        .then(function(n) {
            // TÍTULO:
            // textContent es más seguro que innerHTML para datos del usuario
            // porque no interpreta etiquetas HTML (evita ataques XSS).
            document.getElementById('noticia-titulo').textContent = n.titulo || 'Sin título';

            // METADATOS (autor y fecha):
            var metaEl    = document.getElementById('noticia-meta');
            // Si n.autor está vacío o es null, usamos 'Autor desconocido'.
            var autorTexto = n.autor ? n.autor : 'Autor desconocido';
            // Si hay fecha, la formateamos a texto legible en español.
            // new Date() convierte el String de fecha a objeto Date de JavaScript.
            // toLocaleDateString('es-ES', {...}) → "19 de mayo de 2024"
            var fechaTexto = n.fechaPublicacion
                ? new Date(n.fechaPublicacion).toLocaleDateString('es-ES', {
                    year: 'numeric', month: 'long', day: 'numeric'
                  })
                : '';
            // Insertamos autor y fecha como dos <span> dentro del elemento meta.
            metaEl.innerHTML = '<span>' + autorTexto + '</span><span>' + fechaTexto + '</span>';

            // IMAGEN:
            var imgElement = document.getElementById('noticia-imagen');
            if (n.nombreImagen && n.nombreImagen !== 'null' && n.nombreImagen !== '') {
                // Si tiene imagen → asignamos la URL y la mostramos.
                imgElement.src           = '../uploads/' + n.nombreImagen;
                imgElement.style.display = 'block';
            } else {
                // Sin imagen → ocultamos el elemento <img> completamente.
                imgElement.style.display = 'none';
            }

            // CONTENIDO:
            if (n.contenido) {
                // El contenido puede contener saltos de línea (\n).
                // split('\n') divide el texto en un array de párrafos.
                // map() envuelve cada párrafo en etiquetas <p>.
                // join('') une el array de vuelta a un String sin separadores.
                document.getElementById('noticia-cuerpo').innerHTML =
                    n.contenido.split('\n').map(function(p) {
                        return '<p>' + p + '</p>';
                    }).join('');
            } else {
                document.getElementById('noticia-cuerpo').innerHTML = '<p>Sin contenido.</p>';
            }
        })
        // .catch(): captura dos tipos de errores:
        //   A) Error de red (fetch() falla por sin conexión o servidor caído).
        //   B) El throw new Error() que lanzamos manualmente si res.ok era false.
        .catch(function() {
            document.getElementById('noticia-titulo').textContent = 'Error al cargar la noticia';
        });
}


/* ==========================================================================
   FUNCIONES DE MODAL
   ========================================================================== */
function abrirModal(id) { document.getElementById(id).style.display = 'flex'; }
function cerrarModal(id) { document.getElementById(id).style.display = 'none'; }


/* ==========================================================================
   LOGIN (igual que en app.js, necesario aquí porque esta página
   también tiene modal de login en el header)
   ========================================================================== */
function iniciarSesion(e) {
    // Cancelamos el comportamiento por defecto del formulario (recargar la página).
    e.preventDefault();

    var usuario  = document.getElementById('usuario').value;
    var password = document.getElementById('password').value;

    // POST /api/login con usuario y contraseña en el body.
    fetch('../api/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: 'usuario=' + encodeURIComponent(usuario) + '&password=' + encodeURIComponent(password)
    })
        // .then(): si el servidor devuelve 200 (ok) → redirigimos a admin.html.
        //         si devuelve 401 → avisamos al usuario.
        .then(function(res) {
            if (res.ok) {
                window.location.href = 'admin.html';
            } else {
                alert('Usuario o contraseña incorrectos');
            }
        })
        // .catch(): error de red (sin conexión, servidor caído).
        .catch(function() {
            alert('Error de conexión con el servidor');
        });
}


/* ==========================================================================
   GESTIÓN DE SESIÓN Y UI
   ========================================================================== */

/* --------------------------------------------------------------------------
   FUNCIÓN: comprobarSesion

   Pregunta al servidor si hay admin logueado y adapta la interfaz.
   -------------------------------------------------------------------------- */
function comprobarSesion() {
    fetch('../api/sesion')
        // Primer .then(): parseamos el JSON.
        .then(function(res) {
            return res.json();
        })
        // Segundo .then(): actualizamos la UI con el estado de sesión.
        .then(function(data) {
            actualizarUI(data.logueado);
        })
        // .catch(): si hay error, asumimos que no hay sesión.
        .catch(function() {
            actualizarUI(false);
        });
}

/* --------------------------------------------------------------------------
   FUNCIÓN: actualizarUI

   Muestra u oculta los controles de navegación según si hay admin logueado.
   -------------------------------------------------------------------------- */
function actualizarUI(logueado) {
    var controlesAdmin    = document.getElementById('controles-admin');
    var controlesPublicos = document.getElementById('controles-publicos');

    if (logueado) {
        controlesAdmin.style.display    = 'block';
        controlesPublicos.style.display = 'none';
    } else {
        controlesAdmin.style.display    = 'none';
        controlesPublicos.style.display = 'block';
    }
}


/* ==========================================================================
   REAJUSTE AUTOMÁTICO AL CAMBIAR DE PESTAÑA (REFLOW BUG FIX)
   ========================================================================== */
document.addEventListener('visibilitychange', function() {
    if (document.visibilityState === 'visible') {
        window.dispatchEvent(new Event('resize'));
    }
});

window.addEventListener('pageshow', function() {
    window.dispatchEvent(new Event('resize'));
});