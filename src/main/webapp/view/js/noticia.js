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
    document.getElementById('boton-abrir-login').onclick = function(evento) {
        // Cancelamos el comportamiento por defecto del botón (recargar la página).
        evento.preventDefault();
        abrirModal('ventana-emergente-login');
    };

    document.getElementById('boton-cerrar-login').onclick = function() {
        cerrarModal('ventana-emergente-login');
    };

    document.getElementById('formulario-login').onsubmit = iniciarSesion;
});


/* ==========================================================================
   CARGA DE LA NOTICIA INDIVIDUAL
   ========================================================================== */

/* --------------------------------------------------------------------------
   FUNCIÓN: cargarNoticiaIndividual

   Lee el parámetro "id" de la URL actual, hace un GET a /api/noticias/obtener?id=X
   y muestra el contenido completo de la noticia en el HTML.
   -------------------------------------------------------------------------- */
function cargarNoticiaIndividual() {
    // URLSearchParams analiza los parámetros de la URL de la página actual.
    // Por ejemplo, si la URL es "noticia.html?id=5", window.location.search es "?id=5".
    var parametrosUrl = new URLSearchParams(window.location.search);

    // params.get('id') extrae el valor del parámetro "id".
    // Resultado: la cadena "5" (como texto, no como número).
    var idNoticiaUrl = parametrosUrl.get('id');

    // Referencia al elemento del título para poder modificarlo si hay errores.
    var elementoHtmlTitulo = document.getElementById('titulo-noticia');

    // Si el elemento no existe en el HTML, salimos (prevención de errores).
    if (!elementoHtmlTitulo) return;

    // Si no hay ID en la URL, no sabemos qué noticia cargar. Mostramos error y salimos.
    if (!idNoticiaUrl) {
        elementoHtmlTitulo.textContent = 'Noticia no encontrada';
        return;
    }

    // Hacemos GET al Servlet con el ID como parámetro en la URL.
    // El parámetro ?id=5 lo recibe el Servlet con request.getParameter("id").
    // method: 'GET' porque solo consultamos datos, no los modificamos.
    fetch('../api/noticias/obtener?id=' + idNoticiaUrl, {
        method: 'GET'
    })
        // Primer .then(): comprobamos el código HTTP y parseamos el JSON.
        .then(function(respuestaServidor) {
            // Si el servidor devuelve un error (404, 500...) lanzamos un Error
            // para que lo capture el .catch() de abajo.
            if (!respuestaServidor.ok) {
                throw new Error('Error HTTP: ' + respuestaServidor.status);
            }
            // Retornamos el JSON parseado para el siguiente .then().
            return respuestaServidor.json();
        })
        // Segundo .then(): recibimos el objeto noticia y rellenamos el HTML.
        .then(function(noticiaRecibida) {

            // TÍTULO:
            document.getElementById('titulo-noticia').textContent = noticiaRecibida.titulo;

            // METADATOS (autor y fecha):
            var elementoHtmlMeta = document.getElementById('metadatos-noticia');

            // Decidimos qué texto de autor mostrar.
            var textoAutor;
            if (noticiaRecibida.autor) {
                // La noticia tiene autor → lo mostramos tal cual.
                textoAutor = noticiaRecibida.autor;
            } else {
                // La noticia no tiene autor guardado → mostramos un texto genérico.
                textoAutor = 'Autor desconocido';
            }

            // Decidimos qué texto de fecha mostrar.
            var textoFecha;
            if (noticiaRecibida.fechaPublicacion) {
                // La noticia tiene fecha → la formateamos a texto legible en español.
                // new Date() convierte el texto de fecha a un objeto Date de JavaScript.
                // toLocaleDateString() lo convierte a un texto como "24 de mayo de 2026".
                textoFecha = new Date(noticiaRecibida.fechaPublicacion).toLocaleDateString('es-ES', {
                    year: 'numeric', month: 'long', day: 'numeric'
                });
            } else {
                // La noticia no tiene fecha guardada → mostramos cadena vacía.
                textoFecha = '';
            }

            // Insertamos autor y fecha como dos <span> dentro del elemento de metadatos.
            elementoHtmlMeta.innerHTML = '<span>' + textoAutor + '</span><span>' + textoFecha + '</span>';

            // IMAGEN:
            var elementoHtmlImagen = document.getElementById('campo-archivo-imagen');
            if (noticiaRecibida.nombreImagen && noticiaRecibida.nombreImagen !== 'null' && noticiaRecibida.nombreImagen !== '') {
                // La noticia tiene imagen → asignamos la URL y la mostramos.
                elementoHtmlImagen.src          = '../uploads/' + noticiaRecibida.nombreImagen;
                elementoHtmlImagen.style.display = 'block';
            } else {
                // La noticia no tiene imagen → ocultamos el elemento <img> completamente.
                elementoHtmlImagen.style.display = 'none';
            }

            // CONTENIDO:
            if (noticiaRecibida.contenido) {
                // El contenido puede contener saltos de línea (\n).
                // split('\n') divide el texto en un array de párrafos separados por salto de línea.
                // map() envuelve cada párrafo en etiquetas <p> para que se vea como párrafo en HTML.
                // join('') une el array de vuelta a un String único.
                document.getElementById('cuerpo-noticia').innerHTML =
                    noticiaRecibida.contenido.split('\n').map(function(parrafoIndividual) {
                        return '<p>' + parrafoIndividual + '</p>';
                    }).join('');
            } else {
                // La noticia no tiene contenido → mostramos un mensaje informativo.
                document.getElementById('cuerpo-noticia').innerHTML = '<p>Sin contenido.</p>';
            }
        })
        // .catch(): captura errores de red o errores lanzados manualmente con throw.
        .catch(function(errorDetectado) {
            console.error('Error al cargar la noticia:', errorDetectado);
            document.getElementById('titulo-noticia').textContent = 'Error al cargar la noticia';
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
    // Cancelamos el comportamiento por defecto del formulario (recargar la página).
    eventoSubmit.preventDefault();

    // Leemos los valores escritos por el usuario en los campos del formulario.
    var nombreUsuario     = document.getElementById('campo-usuario').value;
    var contrasenaUsuario = document.getElementById('campo-contrasena').value;

    // method: 'POST' porque enviamos datos sensibles (contraseña) al servidor.
    // encodeURIComponent() convierte caracteres especiales a formato seguro para la URL.
    fetch('../api/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: 'usuario=' + encodeURIComponent(nombreUsuario) + '&password=' + encodeURIComponent(contrasenaUsuario)
    })
        // .then(): gestiona la respuesta del servidor.
        .then(function(respuestaServidor) {
            if (respuestaServidor.ok) {
                // Login correcto → redirigimos al panel de administración.
                window.location.href = 'admin.html';
            } else {
                // Login incorrecto → avisamos al usuario.
                alert('Usuario o contraseña incorrectos');
            }
        })
        // .catch(): error de red (sin internet, servidor caído).
        .catch(function(errorRed) {
            console.error('Error al iniciar sesión:', errorRed);
            alert('Error de conexión con el servidor');
        });
}


/* ==========================================================================
   GESTIÓN DE SESIÓN Y UI
   ========================================================================== */

/* --------------------------------------------------------------------------
   FUNCIÓN: comprobarSesion

   Pregunta al servidor si hay una sesión de admin activa.
   Ajusta la interfaz según el resultado.
   -------------------------------------------------------------------------- */
function comprobarSesion() {
    // method: 'GET' porque solo consultamos el estado de la sesión, no modificamos nada.
    // El Servlet SesionServlet responde con: {"logueado": true/false, "usuario": "..."}
    fetch('../api/sesion', {
        method: 'GET'
    })
        // Primer .then(): parseamos el JSON de la respuesta.
        .then(function(respuestaServidor) {
            return respuestaServidor.json();
        })
        // Segundo .then(): recibimos los datos y actualizamos la interfaz.
        .then(function(datosSesion) {
            actualizarInterfazUsuario(datosSesion.logueado);
        })
        // .catch(): si hay error, asumimos que no está logueado.
        .catch(function(errorDetectado) {
            console.error('Error al comprobar sesión:', errorDetectado);
            actualizarInterfazUsuario(false);
        });
}

/* --------------------------------------------------------------------------
   FUNCIÓN: actualizarInterfazUsuario

   Muestra u oculta los controles del footer según si hay admin logueado o no.
   -------------------------------------------------------------------------- */
function actualizarInterfazUsuario(estaLogueado) {
    var controlesAdministrador = document.getElementById('controles-administrador');    // Botón "Panel Admin"
    var controlesPublico       = document.getElementById('controles-publico'); // Botón "Admin" (Login)

    if (estaLogueado) {
        // Admin logueado → mostramos sus controles y ocultamos el botón de login.
        controlesAdministrador.style.display = 'block';
        controlesPublico.style.display       = 'none';
    } else {
        // Sin sesión activa → mostramos el botón de login y ocultamos los controles de admin.
        controlesAdministrador.style.display = 'none';
        controlesPublico.style.display       = 'block';
    }
}
