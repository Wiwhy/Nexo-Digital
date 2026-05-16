document.addEventListener('DOMContentLoaded', () => {
    comprobarSesionAdmin();

    document.getElementById('btn-nueva-noticia').onclick = () => abrirModalNoticia();
    document.getElementById('btn-cerrar-modal-noticia').onclick = () => cerrarModal('modal-noticia');
    document.getElementById('form-noticia').onsubmit = guardarNoticia;

    document.getElementById('btn-logout').onclick = cerrarSesion;
});

async function comprobarSesionAdmin() {
    try {
        const res = await fetch('../api/sesion');
        const data = await res.json();
        if (!data.logueado) {
            window.location.href = 'index.html';
        } else {
            cargarNoticiasAdmin();
        }
    } catch (err) {
        window.location.href = 'index.html';
    }
}

async function cerrarSesion(e) {
    e.preventDefault();
    try {
        await fetch('../api/sesion', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: 'action=logout'
        });
        window.location.href = 'index.html';
    } catch (err) {
        window.location.href = 'index.html';
    }
}

let noticiasGlobales = [];

async function cargarNoticiasAdmin() {
    try {
        const res = await fetch('../api/noticias/listar');
        noticiasGlobales = await res.json();
        renderTablaNoticias();
    } catch (err) { }
}

function renderTablaNoticias() {
    const contenedor = document.getElementById('lista-noticias');
    contenedor.innerHTML = '';

    noticiasGlobales.forEach(n => {
        const fila = document.createElement('div');
        fila.className = 'fila-noticia';

        fila.innerHTML = `
            <span class="titulo-noticia-lista">${n.titulo}</span>
            <button class="btn-azul btn-pequeno" onclick='editarNoticia(${JSON.stringify(n).replace(/'/g, "&apos;")})'>Editar</button>
        `;
        contenedor.appendChild(fila);
    });
}

function abrirModalNoticia(noticia = null) {
    const form = document.getElementById('form-noticia');
    const btnEliminar = document.getElementById('btn-eliminar-modal');
    const btnGuardar = document.getElementById('btn-guardar-modal');
    form.reset();

    if (noticia) {
        document.getElementById('modal-noticia-titulo').textContent = 'Editar noticia';
        document.getElementById('noticia-id').value = noticia.id;
        document.getElementById('noticia-titulo-input').value = noticia.titulo;
        document.getElementById('noticia-autor-input').value = noticia.autor || '';
        document.getElementById('noticia-contenido').value = noticia.contenido;
        
        const spanImagen = document.getElementById('nombre-imagen-actual');
        if (noticia.nombreImagen && noticia.nombreImagen !== 'null' && noticia.nombreImagen !== '') {
            spanImagen.textContent = 'Imagen actual: ' + noticia.nombreImagen;
        } else {
            spanImagen.textContent = 'Sin imagen actual';
        }

        btnGuardar.textContent = 'Guardar';
        btnEliminar.style.display = 'block';
        btnEliminar.onclick = () => eliminarNoticia(noticia.id);
    } else {
        document.getElementById('modal-noticia-titulo').textContent = 'Crear noticia';
        document.getElementById('noticia-id').value = '';
        document.getElementById('nombre-imagen-actual').textContent = '';
        btnGuardar.textContent = 'Crear';
        btnEliminar.style.display = 'none';
    }
    abrirModal('modal-noticia');
}

function editarNoticia(noticiaObj) {
    abrirModalNoticia(noticiaObj);
}

async function guardarNoticia(e) {
    e.preventDefault();
    const formData = new FormData(e.target);
    const id = formData.get('id');
    const endpoint = id ? '../api/noticias/actualizar' : '../api/noticias';

    try {
        const res = await fetch(endpoint, { method: 'POST', body: formData });
        if (res.ok) {
            cerrarModal('modal-noticia');
            cargarNoticiasAdmin();
        } else {
            const data = await res.json().catch(() => ({}));
            alert('Error: ' + (data.message || res.status));
        }
    } catch (err) { }
}

async function eliminarNoticia(id) {
    if (!confirm('¿Seguro que deseas eliminar esta noticia?')) return;
    try {
        const res = await fetch('../api/noticias/eliminar', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: `id=${id}`
        });
        if (res.ok) {
            cerrarModal('modal-noticia');
            cargarNoticiasAdmin();
        } else {
            alert('Error al eliminar noticia');
        }
    } catch (err) { }
}

function abrirModal(id) { document.getElementById(id).style.display = 'flex'; }
function cerrarModal(id) { document.getElementById(id).style.display = 'none'; }

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