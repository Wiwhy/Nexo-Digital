/* ==========================================================================
   admin.js — JAVASCRIPT DEL PANEL DE ADMINISTRACIÓN (admin.html)

   Este archivo controla la página de administración.
   Solo los administradores autenticados pueden acceder a ella.

   Se encarga de:
     1. Verificar que el usuario es admin al cargar (si no, redirigir a inicio).
     2. Cargar y mostrar la lista de noticias existentes.
     3. Gestionar el modal de creación/edición de noticias.
     4. Guardar (crear o actualizar) noticias.
     5. Eliminar noticias.
     6. Cerrar sesión.
   ========================================================================== */


/* ==========================================================================
   INICIALIZACIÓN AL CARGAR LA PÁGINA
   ========================================================================== */
document.addEventListener('DOMContentLoaded', function() {

    // LO PRIMERO: verificamos si hay sesión de admin.
    // Si no la hay, redirigimos a index.html.
    comprobarSesionAdmin();

    // Botón "Nueva noticia" → abre el modal en modo creación (sin datos previos).
    document.getElementById('boton-nueva-noticia').onclick = function() {
        abrirModalNoticia();
    };

    // Botón "X" (cerrar) del modal → cierra el modal.
    document.getElementById('boton-cerrar-modal-noticia').onclick = function() {
        cerrarModal('ventana-emergente-noticia');
    };

    // Formulario del modal → al enviar, llamamos a guardarNoticia().
    // guardarNoticia decide si es crear o actualizar según el campo "id".
    document.getElementById('formulario-noticia').onsubmit = guardarNoticia;

    // Botón "Cerrar sesión" → llama a cerrarSesion().
    document.getElementById('boton-cerrar-sesion').onclick = cerrarSesion;
});


/* ==========================================================================
   VERIFICACIÓN DE SESIÓN DE ADMIN
   ========================================================================== */

/* --------------------------------------------------------------------------
   FUNCIÓN: comprobarSesionAdmin

   Hace un GET a /api/sesion para saber si hay admin logueado.
   Si no hay → redirige al inicio. Si hay → carga las noticias.
   -------------------------------------------------------------------------- */
function comprobarSesionAdmin() {
    // method: 'GET' porque solo consultamos si hay sesión, no modificamos nada.
    // El Servlet SesionServlet responde con: {"logueado": true/false, "usuario": "..."}
    fetch('../api/sesion', {
        method: 'GET'
    })
        // Primer .then(): parseamos el JSON de la respuesta.
        .then(function(respuestaServidor) {
            return respuestaServidor.json();
        })
        // Segundo .then(): comprobamos si el usuario está logueado como admin.
        .then(function(datosSesion) {
            if (!datosSesion.logueado) {
                // Si NO está logueado → redirigimos al portal público.
                // Esto protege la página de admin de accesos no autorizados.
                window.location.href = 'index.html';
            } else {
                // Si SÍ está logueado → cargamos la lista de noticias.
                cargarNoticiasAdmin();
            }
        })
        // .catch(): si hay error de red → redirigimos al inicio por seguridad.
        .catch(function(errorDetectado) {
            console.error('Error al comprobar sesión de admin:', errorDetectado);
            window.location.href = 'index.html';
        });
}


/* ==========================================================================
   CERRAR SESIÓN
   ========================================================================== */

/* --------------------------------------------------------------------------
   FUNCIÓN: cerrarSesion

   Envía POST a /api/sesion con action=logout para destruir la sesión,
   luego redirige al usuario a la página pública.
   -------------------------------------------------------------------------- */
function cerrarSesion(eventoClick) {
    // Evita que el enlace/botón haga su acción por defecto (navegar o recargar).
    eventoClick.preventDefault();

    // method: 'POST' porque estamos modificando el estado del servidor (destruyendo la sesión).
    // body: "action=logout" le indica al SesionServlet que queremos cerrar sesión.
    fetch('../api/sesion', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: 'action=logout'
    })
        // .then(): tras recibir respuesta del servidor, redirigimos al inicio.
        .then(function(respuestaServidor) {
            window.location.href = 'index.html';
        })
        // .catch(): si hay error de red, redirigimos igualmente al inicio.
        .catch(function(errorDetectado) {
            console.error('Error al cerrar sesión:', errorDetectado);
            window.location.href = 'index.html';
        });
}


/* ==========================================================================
   CARGA Y RENDERIZADO DE LA LISTA DE NOTICIAS
   ========================================================================== */

// Variable global donde almacenamos la lista de noticias cargadas.
// Es global para que renderTablaNoticias() pueda acceder a ella desde fuera de cargarNoticiasAdmin().
var listaNoticiasGlobales = [];

/* --------------------------------------------------------------------------
   FUNCIÓN: cargarNoticiasAdmin

   Hace un GET a /api/noticias/listar, guarda las noticias en la variable
   global y llama a renderTablaNoticias() para mostrarlas en el HTML.
   -------------------------------------------------------------------------- */
function cargarNoticiasAdmin() {
    // method: 'GET' porque solo leemos datos de la base de datos.
    fetch('../api/noticias/listar', {
        method: 'GET'
    })
        // Primer .then(): parseamos el JSON de la respuesta.
        .then(function(respuestaServidor) {
            return respuestaServidor.json();
        })
        // Segundo .then(): guardamos el array en la variable global y renderizamos.
        .then(function(listaDeNoticias) {
            // Guardamos en la variable global para que abrirModalNoticia() pueda acceder.
            listaNoticiasGlobales = listaDeNoticias;
            // Dibujamos la lista en el HTML.
            renderTablaNoticias();
        })
        // .catch(): si hay error de red o JSON inválido, lo mostramos en consola.
        .catch(function(errorDetectado) {
            console.error('Error al cargar noticias:', errorDetectado);
        });
}

/* --------------------------------------------------------------------------
   FUNCIÓN: renderTablaNoticias

   Construye dinámicamente la lista de noticias con un botón "Editar" por cada una.
   -------------------------------------------------------------------------- */
function renderTablaNoticias() {
    var contenedorListaHtml = document.getElementById('lista-noticias');

    // Vaciamos el contenedor antes de renderizar para evitar duplicados.
    contenedorListaHtml.innerHTML = '';

    // Creamos una fila por cada noticia en la lista global.
    listaNoticiasGlobales.forEach(function(noticiaActual) {
        var elementoHtmlFila = document.createElement('div');
        elementoHtmlFila.className = 'fila-noticia';

        // JSON.stringify(noticiaActual) convierte el objeto noticia a String JSON para poder
        // pasarlo como argumento en el onclick del botón.
        // .replace(/'/g, "&apos;") reemplaza comillas simples para no romper el HTML.
        elementoHtmlFila.innerHTML =
            '<span class="titulo-lista-admin">' + noticiaActual.titulo + '</span>' +
            '<button class="btn-azul boton-pequeno" onclick=\'abrirModalNoticia(' +
            JSON.stringify(noticiaActual).replace(/'/g, '&apos;') +
            ')\'>Editar</button>';

        contenedorListaHtml.appendChild(elementoHtmlFila);
    });
}


/* ==========================================================================
   MODAL DE CREACIÓN / EDICIÓN DE NOTICIAS
   ========================================================================== */

/* --------------------------------------------------------------------------
   FUNCIÓN: abrirModalNoticia

   Abre el modal del formulario.
   Si recibe un objeto noticia → modo EDICIÓN (rellena el formulario).
   Si no recibe nada (objetoNoticia = null) → modo CREACIÓN (formulario en blanco).
   -------------------------------------------------------------------------- */
function abrirModalNoticia(objetoNoticia) {
    // Si no se pasa argumento, objetoNoticia será undefined → lo tratamos como null.
    objetoNoticia = objetoNoticia || null;

    var formularioHtml      = document.getElementById('formulario-noticia');
    var botonEliminarHtml   = document.getElementById('boton-eliminar-noticia');
    var botonGuardarHtml    = document.getElementById('boton-guardar-noticia');

    // Limpiamos todos los campos del formulario antes de rellenarlos.
    formularioHtml.reset();

    if (objetoNoticia) {
        // MODO EDICIÓN: rellenamos el formulario con los datos existentes.
        document.getElementById('titulo-ventana-emergente').textContent = 'Editar noticia';

        // Campo oculto con el ID de la noticia (se envía con el formulario).
        // El Servlet lo usa para saber qué fila de la BD actualizar.
        document.getElementById('campo-oculto-id').value              = objetoNoticia.id;
        document.getElementById('campo-titulo').value    = objetoNoticia.titulo;
        document.getElementById('campo-autor').value     = objetoNoticia.autor || '';
        document.getElementById('campo-contenido').value       = objetoNoticia.contenido;

        // Mostramos el nombre del archivo de imagen actual (si tiene).
        var elementoTextoArchivo = document.getElementById('texto-nombre-archivo');
        if (objetoNoticia.nombreImagen && objetoNoticia.nombreImagen !== 'null' && objetoNoticia.nombreImagen !== '') {
            // La noticia tiene imagen → mostramos su nombre de archivo.
            elementoTextoArchivo.textContent = objetoNoticia.nombreImagen;
        } else {
            // La noticia no tiene imagen → mostramos el texto por defecto.
            elementoTextoArchivo.textContent = 'Ningún archivo seleccionado';
        }

        botonGuardarHtml.textContent = 'Guardar';

        // Mostramos el botón de eliminar (solo disponible en modo edición).
        botonEliminarHtml.style.display = 'block';

        // Guardamos el id en una variable local para usarlo en el onclick (closure).
        // Esto es necesario para que el botón "Eliminar" recuerde el ID de esta noticia.
        var idNoticia = objetoNoticia.id;
        botonEliminarHtml.onclick = function() {
            eliminarNoticia(idNoticia);
        };

    } else {
        // MODO CREACIÓN: formulario completamente vacío.
        document.getElementById('titulo-ventana-emergente').textContent = 'Crear noticia';
        document.getElementById('campo-oculto-id').value                 = '';
        document.getElementById('texto-nombre-archivo').textContent      = 'Ningún archivo seleccionado';
        botonGuardarHtml.textContent = 'Crear';

        // Ocultamos el botón de eliminar (no tiene sentido en modo creación).
        botonEliminarHtml.style.display = 'none';
    }

    abrirModal('ventana-emergente-noticia');
}


/* ==========================================================================
   GUARDAR NOTICIA (CREAR O ACTUALIZAR)
   ========================================================================== */

/* --------------------------------------------------------------------------
   FUNCIÓN: guardarNoticia

   Se llama al enviar el formulario del modal.
   Si el campo "id" tiene valor → actualiza una noticia existente (POST a /actualizar).
   Si el campo "id" está vacío → crea una nueva noticia (POST a /api/noticias).
   -------------------------------------------------------------------------- */
function guardarNoticia(eventoSubmit) {
    // Evita que el formulario recargue la página al enviarse.
    eventoSubmit.preventDefault();

    // FormData empaqueta todos los campos del formulario, incluidos archivos.
    // Automáticamente usa multipart/form-data, que los Servlets con @MultipartConfig esperan.
    var datosDelFormulario = new FormData(eventoSubmit.target);

    // Leemos el campo oculto "id":
    //   - Si tiene valor → es una noticia existente → actualizamos.
    //   - Si está vacío  → es nueva               → creamos.
    var idNoticia = datosDelFormulario.get('id');

    // Decidimos la URL de destino según si hay ID o no.
    var urlDestinoBackend;
    if (idNoticia) {
        // La noticia ya existe → enviamos al Servlet de actualizar.
        urlDestinoBackend = '../api/noticias/actualizar';
    } else {
        // La noticia es nueva → enviamos al Servlet de crear.
        urlDestinoBackend = '../api/noticias';
    }

    // method: 'POST' porque enviamos datos al servidor (tanto para crear como para actualizar).
    // No ponemos Content-Type en el header porque FormData lo gestiona automáticamente.
    fetch(urlDestinoBackend, {
        method: 'POST',
        body: datosDelFormulario
    })
        // .then(): comprobamos si el servidor respondió con éxito.
        .then(function(respuestaServidor) {
            if (respuestaServidor.ok) {
                // Éxito → cerramos el modal y recargamos la lista de noticias.
                cerrarModal('ventana-emergente-noticia');
                cargarNoticiasAdmin();
            } else {
                // Error del servidor → intentamos leer el mensaje de error en JSON.
                // Encadenamos otro .then() al respuestaServidor.json() para leer el cuerpo.
                return respuestaServidor.json()
                    .then(function(datosErrorJson) {
                        alert('Error: ' + (datosErrorJson.message || respuestaServidor.status));
                    })
                    // Si el cuerpo no es JSON válido, mostramos el código de estado.
                    .catch(function() {
                        alert('Error: ' + respuestaServidor.status);
                    });
            }
        })
        // .catch(): error de red (servidor caído, sin conexión, etc.)
        .catch(function(errorDetectado) {
            console.error('Error al guardar noticia:', errorDetectado);
        });
}


/* ==========================================================================
   ELIMINAR NOTICIA
   ========================================================================== */

/* --------------------------------------------------------------------------
   FUNCIÓN: eliminarNoticia

   Pide confirmación y, si el usuario acepta, elimina la noticia con el ID dado.
   -------------------------------------------------------------------------- */
function eliminarNoticia(idNoticia) {
    // confirm() muestra un diálogo "Aceptar / Cancelar" al usuario.
    // Si el usuario cancela → confirm() devuelve false → salimos sin hacer nada.
    if (!confirm('¿Seguro que deseas eliminar esta noticia?')) return;

    // method: 'POST' porque modificamos datos en el servidor (borramos una fila de la BD).
    // body: "id=5" → el Servlet lee este valor con request.getParameter("id").
    fetch('../api/noticias/eliminar', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: 'id=' + idNoticia
    })
        // .then(): si el servidor respondió correctamente, actualizamos la vista.
        .then(function(respuestaServidor) {
            if (respuestaServidor.ok) {
                // Cerramos el modal y recargamos la lista (sin la noticia eliminada).
                cerrarModal('ventana-emergente-noticia');
                cargarNoticiasAdmin();
            } else {
                alert('Error al eliminar noticia');
            }
        })
        // .catch(): error de red.
        .catch(function(errorDetectado) {
            console.error('Error al eliminar noticia:', errorDetectado);
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
   ACTUALIZAR TEXTO DEL SELECTOR DE ARCHIVO

   El input de tipo "file" tiene apariencia nativa poco personalizable.
   Mostramos el nombre del archivo seleccionado en un <span> propio.
   ========================================================================== */
document.getElementById('campo-archivo-imagen').addEventListener('change', function() {
    var elementoTextoArchivo = document.getElementById('texto-nombre-archivo');

    // "this" hace referencia al input de archivo que disparó el evento.
    // "this.files" es la lista de archivos seleccionados (FileList).
    if (this.files && this.files.length > 0) {
        // El usuario seleccionó al menos un archivo → mostramos el nombre del primero.
        elementoTextoArchivo.textContent = this.files[0].name;
    } else {
        // No hay archivos seleccionados → mostramos el texto por defecto.
        elementoTextoArchivo.textContent = 'Ningún archivo seleccionado';
    }
});
