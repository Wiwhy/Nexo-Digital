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

// Cuando el DOM esté listo, ejecutamos la inicialización.
document.addEventListener('DOMContentLoaded', function() {

    // LO PRIMERO: verificamos si hay sesión de admin.
    // Si no la hay, redirigimos a index.html.
    comprobarSesionAdmin();

    // Botón "Nueva noticia" → abre el modal en modo creación (sin datos previos).
    document.getElementById('btn-nueva-noticia').onclick = function() {
        abrirModalNoticia();
    };

    // Botón "X" (cerrar) del modal → cierra el modal.
    document.getElementById('btn-cerrar-modal-noticia').onclick = function() {
        cerrarModal('modal-noticia');
    };

    // Formulario del modal → al enviar, llamamos a guardarNoticia().
    // guardarNoticia decide si es crear o actualizar según el campo "id".
    document.getElementById('form-noticia').onsubmit = guardarNoticia;

    // Botón "Cerrar sesión" → llama a cerrarSesion().
    document.getElementById('btn-logout').onclick = cerrarSesion;
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
    // GET /api/sesion → SesionServlet devuelve {"logueado": true/false, "usuario": "..."}
    fetch('../api/sesion')
        // Primer .then(): parseamos el JSON de la respuesta.
        .then(function(res) {
            return res.json();
        })
        // Segundo .then(): comprobamos si el usuario está logueado como admin.
        .then(function(data) {
            if (!data.logueado) {
                // Si NO está logueado → redirigimos al portal público.
                // Esto protege la página de admin de accesos no autorizados.
                window.location.href = 'index.html';
            } else {
                // Si SÍ está logueado → cargamos la lista de noticias.
                cargarNoticiasAdmin();
            }
        })
        // .catch(): si hay error de red → redirigimos al inicio por seguridad.
        .catch(function() {
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
function cerrarSesion(e) {
    // Evita que el enlace/botón haga su acción por defecto (navegar o recargar).
    e.preventDefault();

    // POST /api/sesion con body "action=logout" → SesionServlet invalida la sesión.
    fetch('../api/sesion', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: 'action=logout'
    })
        // .then(): tras recibir respuesta del servidor, redirigimos al inicio.
        .then(function() {
            window.location.href = 'index.html';
        })
        // .catch(): si hay error de red, redirigimos igualmente al inicio.
        .catch(function() {
            window.location.href = 'index.html';
        });
}


/* ==========================================================================
   CARGA Y RENDERIZADO DE LA LISTA DE NOTICIAS
   ========================================================================== */

// Variable global donde almacenamos el array de noticias cargadas.
// "var" (o "let") fuera de funciones → es accesible desde cualquier función de este archivo.
// Empieza vacío [] y se rellena cuando cargarNoticiasAdmin() tiene éxito.
var noticiasGlobales = [];

/* --------------------------------------------------------------------------
   FUNCIÓN: cargarNoticiasAdmin

   Hace un GET a /api/noticias/listar, guarda las noticias en la variable
   global y llama a renderTablaNoticias() para mostrarlas en el HTML.
   -------------------------------------------------------------------------- */
function cargarNoticiasAdmin() {
    fetch('../api/noticias/listar')
        // Primer .then(): parseamos el JSON de la respuesta.
        .then(function(res) {
            return res.json();
        })
        // Segundo .then(): guardamos el array en la variable global y renderizamos.
        .then(function(noticias) {
            // Guardamos en la variable global para que editarNoticia() pueda acceder.
            noticiasGlobales = noticias;
            // Dibujamos la lista en el HTML.
            renderTablaNoticias();
        })
        // .catch(): si hay error de red o JSON inválido, lo mostramos en consola.
        .catch(function(err) {
            console.error('Error al cargar noticias:', err);
        });
}

/* --------------------------------------------------------------------------
   FUNCIÓN: renderTablaNoticias

   Construye dinámicamente la lista de noticias con un botón "Editar" por cada una.
   -------------------------------------------------------------------------- */
function renderTablaNoticias() {
    var contenedor = document.getElementById('lista-noticias');

    // Vaciamos el contenedor antes de renderizar para evitar duplicados.
    contenedor.innerHTML = '';

    // Creamos una fila por cada noticia en el array global.
    noticiasGlobales.forEach(function(n) {
        var fila = document.createElement('div');
        fila.className = 'fila-noticia';

        // JSON.stringify(n) convierte el objeto noticia a String JSON para poder
        // pasarlo como argumento en el onclick del botón.
        // .replace(/'/g, "&apos;") reemplaza comillas simples para no romper el HTML.
        fila.innerHTML =
            '<span class="titulo-noticia-lista">' + n.titulo + '</span>' +
            '<button class="btn-azul btn-pequeno" onclick=\'abrirModalNoticia(' +
            JSON.stringify(n).replace(/'/g, '&apos;') +
            ')\'>Editar</button>';

        contenedor.appendChild(fila);
    });
}


/* ==========================================================================
   MODAL DE CREACIÓN / EDICIÓN DE NOTICIAS
   ========================================================================== */

/* --------------------------------------------------------------------------
   FUNCIÓN: abrirModalNoticia

   Abre el modal del formulario.
   Si recibe un objeto noticia → modo EDICIÓN (rellena el formulario).
   Si no recibe nada (noticia = null) → modo CREACIÓN (formulario en blanco).
   -------------------------------------------------------------------------- */
function abrirModalNoticia(noticia) {
    // Si no se pasa argumento, noticia será undefined → lo tratamos como null.
    noticia = noticia || null;

    var form       = document.getElementById('form-noticia');
    var btnEliminar = document.getElementById('btn-eliminar-modal');
    var btnGuardar  = document.getElementById('btn-guardar-modal');

    // Limpiamos todos los campos del formulario.
    form.reset();

    if (noticia) {
        // MODO EDICIÓN: rellenamos el formulario con los datos existentes.
        document.getElementById('modal-noticia-titulo').textContent = 'Editar noticia';

        // Campo oculto con el ID de la noticia (se envía con el formulario).
        // El Servlet lo usa para saber qué fila de la BD actualizar.
        document.getElementById('noticia-id').value = noticia.id;

        document.getElementById('noticia-titulo-input').value  = noticia.titulo;
        document.getElementById('noticia-autor-input').value   = noticia.autor || '';
        document.getElementById('noticia-contenido').value     = noticia.contenido;

        // Mostramos el nombre del archivo de imagen actual (si tiene).
        var fileText = document.getElementById('file-input-text');
        if (noticia.nombreImagen && noticia.nombreImagen !== 'null' && noticia.nombreImagen !== '') {
            fileText.textContent = noticia.nombreImagen;
        } else {
            fileText.textContent = 'Ningún archivo seleccionado';
        }

        btnGuardar.textContent = 'Guardar';

        // Mostramos el botón de eliminar (solo disponible en modo edición).
        btnEliminar.style.display = 'block';

        // Guardamos el id en una variable local para usarlo en el onclick (closure).
        var noticiaId = noticia.id;
        btnEliminar.onclick = function() {
            eliminarNoticia(noticiaId);
        };

    } else {
        // MODO CREACIÓN: formulario completamente vacío.
        document.getElementById('modal-noticia-titulo').textContent = 'Crear noticia';
        document.getElementById('noticia-id').value              = '';
        document.getElementById('file-input-text').textContent   = 'Ningún archivo seleccionado';
        btnGuardar.textContent = 'Crear';

        // Ocultamos el botón de eliminar (no tiene sentido en modo creación).
        btnEliminar.style.display = 'none';
    }

    abrirModal('modal-noticia');
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
function guardarNoticia(e) {
    // Evita que el formulario recargue la página al enviarse.
    e.preventDefault();

    // FormData empaqueta todos los campos del formulario, incluidos archivos.
    // Automáticamente usa multipart/form-data, que los Servlets con @MultipartConfig esperan.
    var formData = new FormData(e.target);

    // Leemos el campo oculto "id":
    //   - Si tiene valor → es una noticia existente → actualizamos.
    //   - Si está vacío → es nueva → creamos.
    var id = formData.get('id');
    var endpoint = id ? '../api/noticias/actualizar' : '../api/noticias';

    // POST al endpoint correspondiente con el formulario como body.
    // No ponemos Content-Type → el navegador lo establece automáticamente
    // como 'multipart/form-data; boundary=...' al detectar un FormData.
    fetch(endpoint, { method: 'POST', body: formData })
        // .then(): comprobamos si el servidor respondió con éxito.
        .then(function(res) {
            if (res.ok) {
                // Éxito → cerramos el modal y recargamos la lista de noticias.
                cerrarModal('modal-noticia');
                cargarNoticiasAdmin();
            } else {
                // Error del servidor → intentamos leer el mensaje de error en JSON.
                // Encadenamos otro .then() al res.json() para leer el cuerpo.
                return res.json()
                    .then(function(data) {
                        alert('Error: ' + (data.message || res.status));
                    })
                    // Si el cuerpo no es JSON válido, mostramos el código de estado.
                    .catch(function() {
                        alert('Error: ' + res.status);
                    });
            }
        })
        // .catch(): error de red (servidor caído, sin conexión, etc.)
        .catch(function(err) {
            console.error('Error al guardar noticia:', err);
        });
}


/* ==========================================================================
   ELIMINAR NOTICIA
   ========================================================================== */

/* --------------------------------------------------------------------------
   FUNCIÓN: eliminarNoticia

   Pide confirmación y, si el usuario acepta, elimina la noticia con el ID dado.
   -------------------------------------------------------------------------- */
function eliminarNoticia(id) {
    // confirm() muestra un diálogo "Aceptar / Cancelar".
    // Si el usuario cancela → confirm() devuelve false → salimos sin hacer nada.
    if (!confirm('¿Seguro que deseas eliminar esta noticia?')) return;

    // POST /api/noticias/eliminar con el ID como parámetro en el body.
    // Usamos application/x-www-form-urlencoded (formato simple, sin archivos).
    fetch('../api/noticias/eliminar', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: 'id=' + id
    })
        // .then(): si el servidor respondió correctamente, actualizamos la vista.
        .then(function(res) {
            if (res.ok) {
                // Cerramos el modal y recargamos la lista (sin la noticia eliminada).
                cerrarModal('modal-noticia');
                cargarNoticiasAdmin();
            } else {
                alert('Error al eliminar noticia');
            }
        })
        // .catch(): error de red.
        .catch(function(err) {
            console.error('Error al eliminar noticia:', err);
        });
}


/* ==========================================================================
   FUNCIONES DE MODAL
   ========================================================================== */
function abrirModal(id) { document.getElementById(id).style.display = 'flex'; }
function cerrarModal(id) { document.getElementById(id).style.display = 'none'; }


/* ==========================================================================
   ACTUALIZAR TEXTO DEL SELECTOR DE ARCHIVO

   El input de tipo "file" tiene apariencia nativa poco personalizable.
   Mostramos el nombre del archivo seleccionado en un <span> propio.
   ========================================================================== */
document.getElementById('noticia-imagen').addEventListener('change', function() {
    var fileText = document.getElementById('file-input-text');

    // "this" hace referencia al input de archivo que disparó el evento.
    // "this.files" es la lista de archivos seleccionados (FileList).
    if (this.files && this.files.length > 0) {
        // Mostramos el nombre del primer archivo seleccionado.
        fileText.textContent = this.files[0].name;
    } else {
        fileText.textContent = 'Ningún archivo seleccionado';
    }
});