document.addEventListener('DOMContentLoaded', () => {
    cargarNoticiaIndividual();
    comprobarSesion();

    document.getElementById('btn-abrir-login').onclick = () => abrirModal('modal-login');
    document.getElementById('btn-cerrar-login').onclick = () => cerrarModal('modal-login');
    document.getElementById('form-login').onsubmit = iniciarSesion;
});

async function cargarNoticiaIndividual() {
    const params = new URLSearchParams(window.location.search);
    const idParam = params.get('id');

    const tituloEl = document.getElementById('noticia-titulo');
    if (!tituloEl) return;

    if (!idParam) {
        tituloEl.textContent = 'Noticia no encontrada';
        return;
    }

    try {
        const res = await fetch(`../api/noticias/obtener?id=${idParam}`);

        if (!res.ok) {
            document.getElementById('noticia-titulo').textContent = 'Error al cargar la noticia';
            return;
        }

        const n = await res.json();

        document.getElementById('noticia-titulo').textContent = n.titulo || 'Sin título';

        const metaEl = document.getElementById('noticia-meta');
        const autorTexto = n.autor ? n.autor : 'Autor desconocido';
        const fechaTexto = n.fechaPublicacion ? new Date(n.fechaPublicacion).toLocaleDateString('es-ES', { year: 'numeric', month: 'long', day: 'numeric' }) : '';
        metaEl.innerHTML = `<span>${autorTexto}</span><span>${fechaTexto}</span>`;

        const imgElement = document.getElementById('noticia-imagen');
        if (n.nombreImagen && n.nombreImagen !== 'null' && n.nombreImagen !== '') {
            imgElement.src = `../uploads/${n.nombreImagen}`;
            imgElement.style.display = 'block';
        } else {
            imgElement.style.display = 'none';
        }

        if (n.contenido) {
            document.getElementById('noticia-cuerpo').innerHTML =
                n.contenido.split('\n').map(p => `<p>${p}</p>`).join('');
        } else {
            document.getElementById('noticia-cuerpo').innerHTML = '<p>Sin contenido.</p>';
        }

    } catch (e) {
        document.getElementById('noticia-titulo').textContent = 'Error de conexión';
    }
}

function abrirModal(id) { document.getElementById(id).style.display = 'flex'; }
function cerrarModal(id) { document.getElementById(id).style.display = 'none'; }

async function iniciarSesion(e) {
    e.preventDefault();
    const usuario = document.getElementById('usuario').value;
    const password = document.getElementById('password').value;

    try {
        const res = await fetch('../api/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: `usuario=${encodeURIComponent(usuario)}&password=${encodeURIComponent(password)}`
        });

        if (res.ok) window.location.href = 'admin.html';
        else alert('Usuario o contraseña incorrectos');
    } catch (err) {
        alert('Error de conexión con el servidor');
    }
}

async function comprobarSesion() {
    try {
        const res = await fetch('../api/sesion');
        const data = await res.json();
        actualizarUI(data.logueado);
    } catch (err) {
        actualizarUI(false);
    }
}

function actualizarUI(logueado) {
    const controlesAdmin = document.getElementById('controles-admin');
    const controlesPublicos = document.getElementById('controles-publicos');

    if (logueado) {
        controlesAdmin.style.display = 'block';
        controlesPublicos.style.display = 'none';
    } else {
        controlesAdmin.style.display = 'none';
        controlesPublicos.style.display = 'block';
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
