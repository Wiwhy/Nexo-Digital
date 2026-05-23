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

    // Carga los datos de la noticia cuyo ID viene en la URL.
    cargarNoticiaIndividual();
    
    // Comprueba si hay admin logueado (para mostrar/ocultar controles de admin).
    comprobarSesion();

    // Eventos del modal de login (igual que en app.js).
    document.getElementById('boton-abrir-login').onclick = function(evento) {
        evento.preventDefault();
        abrirModal('modal-login');
    };
    
    document.getElementById('boton-cerrar-login').onclick = function() {
        cerrarModal('modal-login');
    };
    
    document.getElementById('form-login').onsubmit = iniciarSesion;
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
    var parametrosDeLaUrl = new URLSearchParams(window.location.search);

    // params.get('id') extrae el valor del parámetro "id".
    var idDeLaNoticiaEnUrl = parametrosDeLaUrl.get('id');

    // Referencia al elemento del título para poder modificarlo.
    var elementoHtmlTitulo = document.getElementById('noticia-titulo');

    // Si el elemento no existe en el HTML, salimos (prevención de errores).
    if (!elementoHtmlTitulo) return;

    // Si no hay ID en la URL, no sabemos qué noticia cargar.
    if (!idDeLaNoticiaEnUrl) {
        elementoHtmlTitulo.textContent = 'Noticia no encontrada';
        return;
    }

    // Hacemos GET al Servlet con el ID como parámetro en la URL.
    fetch('../api/noticias/obtener?id=' + idDeLaNoticiaEnUrl)
        // Primer .then(): comprobamos el código HTTP y parseamos el JSON.
        .then(function(respuestaServidor) {
            // Si el servidor devuelve un error (404, 500...) lanzamos un Error.
            if (!respuestaServidor.ok) {
                throw new Error('HTTP ' + respuestaServidor.status);
            }
            // Retornamos el JSON parseado.
            return respuestaServidor.json();
        })
        // Segundo .then(): recibimos el objeto noticia y rellenamos el HTML.
        .then(function(noticiaRecibida) {
            // TÍTULO:
            document.getElementById('noticia-titulo').textContent = noticiaRecibida.titulo || 'Sin título';

            // METADATOS (autor y fecha):
            var elementoHtmlMeta = document.getElementById('noticia-meta');
            
            // Si autor está vacío o es null, usamos 'Autor desconocido'.
            var textoAutor = noticiaRecibida.autor ? noticiaRecibida.autor : 'Autor desconocido';
            
            // Si hay fecha, la formateamos a texto legible en español.
            var textoFecha = noticiaRecibida.fechaPublicacion
                ? new Date(noticiaRecibida.fechaPublicacion).toLocaleDateString('es-ES', {
                    year: 'numeric', month: 'long', day: 'numeric'
                  })
                : '';
                
            // Insertamos autor y fecha como dos <span>.
            elementoHtmlMeta.innerHTML = '<span>' + textoAutor + '</span><span>' + textoFecha + '</span>';

            // IMAGEN:
            var elementoHtmlImagen = document.getElementById('noticia-imagen');
            if (noticiaRecibida.nombreImagen && noticiaRecibida.nombreImagen !== 'null' && noticiaRecibida.nombreImagen !== '') {
                // Si tiene imagen → asignamos la URL y la mostramos.
                elementoHtmlImagen.src = '../uploads/' + noticiaRecibida.nombreImagen;
                elementoHtmlImagen.style.display = 'block';
            } else {
                // Sin imagen → ocultamos el elemento <img> completamente.
                elementoHtmlImagen.style.display = 'none';
            }

            // CONTENIDO:
            if (noticiaRecibida.contenido) {
                // El contenido puede contener saltos de línea (\n).
                // split('\n') divide el texto en un array de párrafos.
                // map() envuelve cada párrafo en etiquetas <p>.
                // join('') une el array de vuelta a un String.
                document.getElementById('noticia-cuerpo').innerHTML =
                    noticiaRecibida.contenido.split('\n').map(function(parrafoIndividual) {
                        return '<p>' + parrafoIndividual + '</p>';
                    }).join('');
            } else {
                document.getElementById('noticia-cuerpo').innerHTML = '<p>Sin contenido.</p>';
            }
        })
        // .catch(): captura errores de red o errores lanzados manualmente.
        .catch(function(errorDetectado) {
            document.getElementById('noticia-titulo').textContent = 'Error al cargar la noticia';
        });
}


/* ==========================================================================
   FUNCIONES DE MODAL
   ========================================================================== */
function abrirModal(idDelModal) { 
    document.getElementById(idDelModal).style.display = 'flex'; 
}

function cerrarModal(idDelModal) { 
    document.getElementById(idDelModal).style.display = 'none'; 
}


/* ==========================================================================
   LOGIN (igual que en app.js)
   ========================================================================== */
function iniciarSesion(eventoSubmit) {
    // Cancelamos el recargo de página.
    eventoSubmit.preventDefault();

    var nombreUsuario = document.getElementById('usuario').value;
    var contrasenaUsuario = document.getElementById('password').value;

    // POST /api/login con usuario y contraseña en el body.
    fetch('../api/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: 'usuario=' + encodeURIComponent(nombreUsuario) + '&password=' + encodeURIComponent(contrasenaUsuario)
    })
        // .then(): gestiona la respuesta.
        .then(function(respuestaServidor) {
            if (respuestaServidor.ok) {
                window.location.href = 'admin.html';
            } else {
                alert('Usuario o contraseña incorrectos');
            }
        })
        // .catch(): error de red.
        .catch(function(errorDeRed) {
            alert('Error de conexión con el servidor');
        });
}


/* ==========================================================================
   GESTIÓN DE SESIÓN Y UI
   ========================================================================== */

/* --------------------------------------------------------------------------
   FUNCIÓN: comprobarSesion
   -------------------------------------------------------------------------- */
function comprobarSesion() {
    fetch('../api/sesion')
        .then(function(respuestaServidor) {
            return respuestaServidor.json();
        })
        .then(function(datosSesion) {
            actualizarInterfazUsuario(datosSesion.logueado);
        })
        .catch(function(errorDetectado) {
            actualizarInterfazUsuario(false);
        });
}

/* --------------------------------------------------------------------------
   FUNCIÓN: actualizarInterfazUsuario
   -------------------------------------------------------------------------- */
function actualizarInterfazUsuario(estaLogueado) {
    var controlesDelAdministrador = document.getElementById('controles-admin');
    var controlesDelPublico = document.getElementById('controles-publicos');

    if (estaLogueado) {
        controlesDelAdministrador.style.display = 'block';
        controlesDelPublico.style.display = 'none';
    } else {
        controlesDelAdministrador.style.display = 'none';
        controlesDelPublico.style.display = 'block';
    }
}


