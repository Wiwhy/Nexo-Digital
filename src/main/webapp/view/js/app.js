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


    // Primero cargamos las noticias. Cuando terminen, comprobamos la sesión
    // para mostrar u ocultar los controles de admin en el header.
    // Usamos .then() para asegurarnos de que comprobarSesion() se ejecute
    // DESPUÉS de que las noticias ya estén en el DOM.
    cargarNoticias().then(function() {
        comprobarSesion();
    });

    // Al hacer clic en "Iniciar sesión" → abrimos el modal de login.
    document.getElementById('boton-abrir-login').onclick = function(evento) {
        evento.preventDefault();
        abrirModal('modal-login');
    };

    // Al hacer clic en el botón de cerrar → cerramos el modal.
    document.getElementById('boton-cerrar-login').onclick = function() {
        cerrarModal('modal-login');
    };

    // Al enviar el formulario de login → llamamos a iniciarSesion().
    document.getElementById('form-login').onsubmit = iniciarSesion;
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
        // PRIMER .then(): recibe la respuesta HTTP y parsea el JSON.
        .then(function(respuestaHttp) {
            return respuestaHttp.json();
        })
        // SEGUNDO .then(): recibe la lista de noticias ya convertida a objetos JavaScript.
        .then(function(listaDeNoticias) {
            // Buscamos el contenedor de la cuadrícula de noticias en el HTML.
            var contenedorCuadricula = document.getElementById('grid-todas');

            // Si el elemento no existe (prevención de errores), salimos.
            if (!contenedorCuadricula) return;

            // Vaciamos el contenedor antes de insertar para evitar duplicados.
            contenedorCuadricula.innerHTML = '';

            // forEach() recorre cada noticia de la lista.
            listaDeNoticias.forEach(function(noticiaActual) {
                // Creamos un elemento <article> en memoria (no está en la página aún).
                var articuloHtml = document.createElement('article');

                // Asignamos la clase CSS para dar estilo de tarjeta.
                articuloHtml.className = 'tarjeta-noticia';

                // Si la noticia tiene imagen → usamos su URL. Si no → usamos una por defecto.
                var rutaImagen = (noticiaActual.nombreImagen && noticiaActual.nombreImagen !== 'null' && noticiaActual.nombreImagen !== '')
                    ? '../uploads/' + noticiaActual.nombreImagen
                    : 'https://placehold.co/600x400/1a1a2e/e0e0e0?text=Nexo+Digital';

                // Escribimos el HTML interno de la tarjeta con la imagen y el título.
                articuloHtml.innerHTML =
                    '<img src="' + rutaImagen + '" class="imagen-placeholder" alt="' + noticiaActual.titulo + '">' +
                    '<div class="contenido-tarjeta"><h3>' + noticiaActual.titulo + '</h3></div>';

                // Al hacer clic en la tarjeta, navegamos a la página de la noticia completa.
                // Usamos una función anónima autoejecutable para "recordar" el ID correcto.
                (function(idDeLaNoticia) {
                    articuloHtml.onclick = function() {
                        window.location.href = 'noticia.html?id=' + idDeLaNoticia;
                    };
                })(noticiaActual.id);

                // Insertamos la tarjeta en el contenedor de la cuadrícula.
                contenedorCuadricula.appendChild(articuloHtml);
            });
        })
        // .catch(): captura cualquier error de red o de parseo de JSON.
        .catch(function(errorDetectado) {
            console.error('Error al cargar noticias:', errorDetectado);
        });
}


/* --------------------------------------------------------------------------
   FUNCIONES DE MODAL

   Un modal es una ventana emergente que se controla con CSS:
   display 'flex' lo muestra, 'none' lo oculta.
   -------------------------------------------------------------------------- */
function abrirModal(idDelModal) { 
    document.getElementById(idDelModal).style.display = 'flex'; 
}

function cerrarModal(idDelModal) { 
    document.getElementById(idDelModal).style.display = 'none'; 
}


/* ==========================================================================
   SISTEMA DE SESIONES Y LOGIN
   ========================================================================== */

/* --------------------------------------------------------------------------
   FUNCIÓN: iniciarSesion

   Se llama al enviar el formulario de login.
   Envía usuario y contraseña al servidor con POST y gestiona la respuesta.
   -------------------------------------------------------------------------- */
function iniciarSesion(eventoSubmit) {
    // Cancelamos el comportamiento por defecto del formulario (recargar la página).
    // Queremos manejar el envío mediante JavaScript (AJAX).
    eventoSubmit.preventDefault();

    // Leemos los valores escritos por el usuario en los campos del formulario.
    var nombreUsuario = document.getElementById('usuario').value;
    var contrasenaUsuario = document.getElementById('password').value;

    // Enviamos la petición POST al Servlet de Login.
    fetch('../api/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: 'usuario=' + encodeURIComponent(nombreUsuario) + '&password=' + encodeURIComponent(contrasenaUsuario)
    })
        // .then(): recibe la respuesta del servidor.
        // res.ok es true si el código HTTP es 200 (éxito).
        .then(function(respuestaServidor) {
            if (respuestaServidor.ok) {
                // Login correcto → redirigimos al panel de administración.
                window.location.href = 'admin.html';
            } else {
                // Login incorrecto → avisamos al usuario.
                alert('Usuario o contraseña incorrectos');
            }
        })
        // .catch(): se ejecuta si hay error de RED (sin internet, servidor caído).
        .catch(function(errorDeRed) {
            console.error('Error al iniciar sesión:', errorDeRed);
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
        .then(function(respuestaServidor) {
            return respuestaServidor.json();
        })
        // Segundo .then(): recibimos los datos de la sesión y actualizamos la interfaz.
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

   Muestra u oculta los controles de la barra de navegación según si hay
   un administrador logueado o no.
   -------------------------------------------------------------------------- */
function actualizarInterfazUsuario(estaLogueado) {
    // Referencia a los dos grupos de controles del header.
    var controlesDelAdministrador = document.getElementById('controles-admin');    // "Panel Admin"
    var controlesDelPublico = document.getElementById('controles-publicos'); // "Admin" (Login)

    if (estaLogueado) {
        // Admin logueado → mostramos sus controles y ocultamos los públicos.
        controlesDelAdministrador.style.display = 'block';
        controlesDelPublico.style.display = 'none';
    } else {
        // Sin sesión → mostramos los controles públicos y ocultamos los de admin.
        controlesDelAdministrador.style.display = 'none';
        controlesDelPublico.style.display = 'block';
    }
}


