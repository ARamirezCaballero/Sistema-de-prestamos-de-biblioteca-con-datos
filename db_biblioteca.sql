CREATE DATABASE biblioteca_db;
USE biblioteca_db;

CREATE TABLE Usuario (
    id_usuario INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    dni VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(150) NOT NULL UNIQUE,
    telefono VARCHAR(20),
    fecha_registro DATE NOT NULL,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    tipo_usuario VARCHAR(20) NOT NULL
);

CREATE TABLE Socio (
    id_socio INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT NOT NULL,
    numero_socio VARCHAR(20) NOT NULL UNIQUE,
    fecha_vencimiento_carnet DATE NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    tiene_sanciones BOOLEAN NOT NULL DEFAULT FALSE,
    tiene_atrasos BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (id_usuario) REFERENCES Usuario(id_usuario)
);

CREATE TABLE Bibliotecario (
    id_bibliotecario INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT NOT NULL,
    legajo VARCHAR(20) NOT NULL UNIQUE,
    turno VARCHAR(20) NOT NULL,
    FOREIGN KEY (id_usuario) REFERENCES Usuario(id_usuario)
);

CREATE TABLE PoliticaPrestamo (
    id_politica INT AUTO_INCREMENT PRIMARY KEY,
    categoria VARCHAR(50) NOT NULL UNIQUE,
    dias_prestamo INT NOT NULL,
    max_prestamos_simultaneos INT NOT NULL,
    multa_por_dia DECIMAL(10,2) NOT NULL
);

CREATE TABLE Autor (
    id_autor INT AUTO_INCREMENT PRIMARY KEY,
    nombre_completo VARCHAR(255) NOT NULL UNIQUE,
    nacionalidad VARCHAR(100),
    fecha_nacimiento DATE
);

CREATE TABLE Editorial (
    id_editorial INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL UNIQUE,
    pais VARCHAR(100)
);

CREATE TABLE Libro (
    id_libro INT AUTO_INCREMENT PRIMARY KEY,
    titulo VARCHAR(255) NOT NULL,
    id_autor INT NOT NULL,
    isbn VARCHAR(20) UNIQUE,
    categoria VARCHAR(100),
    id_editorial INT NOT NULL,
    anio_publicacion INT,
    FOREIGN KEY (id_autor) REFERENCES Autor(id_autor),
    FOREIGN KEY (id_editorial) REFERENCES Editorial(id_editorial)
);

CREATE TABLE Ejemplar (
    id_ejemplar INT AUTO_INCREMENT PRIMARY KEY,
    codigo_ejemplar VARCHAR(50) NOT NULL UNIQUE,
    estado VARCHAR(20) NOT NULL DEFAULT 'DISPONIBLE',
    ubicacion VARCHAR(100),
    id_libro INT NOT NULL,
    FOREIGN KEY (id_libro) REFERENCES Libro(id_libro)
);

CREATE TABLE Prestamo (
    id_prestamo INT AUTO_INCREMENT PRIMARY KEY,
    fecha_prestamo DATETIME NOT NULL,
    fecha_vencimiento DATE NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    dias_prestamo INT NOT NULL,
    id_socio INT NOT NULL,
    id_ejemplar INT NOT NULL,
    id_bibliotecario INT NOT NULL,
    FOREIGN KEY (id_socio) REFERENCES Socio(id_socio),
    FOREIGN KEY (id_ejemplar) REFERENCES Ejemplar(id_ejemplar),
    FOREIGN KEY (id_bibliotecario) REFERENCES Bibliotecario(id_bibliotecario)
);

CREATE TABLE Devolucion (
    id_devolucion INT AUTO_INCREMENT PRIMARY KEY,
    fecha_devolucion DATETIME NOT NULL,
    estado_ejemplar VARCHAR(20) NOT NULL DEFAULT 'BUENO',
    observaciones TEXT,
    multa DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    id_prestamo INT NOT NULL UNIQUE,
    FOREIGN KEY (id_prestamo) REFERENCES Prestamo(id_prestamo)
);

CREATE TABLE Comprobante (
    id_comprobante INT AUTO_INCREMENT PRIMARY KEY,
    fecha_emision DATETIME NOT NULL,
    tipo VARCHAR(20) NOT NULL DEFAULT 'DIGITAL',
    contenido TEXT NOT NULL,
    id_prestamo INT NOT NULL UNIQUE,
    FOREIGN KEY (id_prestamo) REFERENCES Prestamo(id_prestamo)
);

CREATE TABLE Notificacion (
    id_notificacion INT AUTO_INCREMENT PRIMARY KEY,
    fecha_envio DATETIME NOT NULL,
    tipo VARCHAR(50) NOT NULL,
    mensaje TEXT NOT NULL,
    leida BOOLEAN NOT NULL DEFAULT FALSE,
    id_prestamo INT NOT NULL,
    FOREIGN KEY (id_prestamo) REFERENCES Prestamo(id_prestamo)
);

CREATE TABLE Historial (
    id_historial INT AUTO_INCREMENT PRIMARY KEY,
    fecha DATETIME NOT NULL,
    tipo_operacion VARCHAR(50) NOT NULL,
    detalles TEXT,
    id_usuario INT NOT NULL,
    id_libro INT NOT NULL,
    id_prestamo INT,
    FOREIGN KEY (id_usuario) REFERENCES Usuario(id_usuario),
    FOREIGN KEY (id_libro) REFERENCES Libro(id_libro),
    FOREIGN KEY (id_prestamo) REFERENCES Prestamo(id_prestamo)
);

ALTER TABLE Socio
ADD COLUMN categoria VARCHAR(50) NOT NULL DEFAULT 'Estándar';

-- Inserción de datos

INSERT INTO Usuario (nombre, apellido, dni, email, telefono, fecha_registro, username, password, tipo_usuario) VALUES
('Juan', 'Pérez', '12345678', 'juan.perez@email.com', '3874123456', '2025-01-15', 'jperez', 'pass123', 'SOCIO'),
('María', 'González', '23456789', 'maria.gonzalez@email.com', '3874234567', '2025-02-10', 'mgonzalez', 'pass123', 'SOCIO'),
('Pedro', 'Rodríguez', '34567890', 'pedro.rodriguez@email.com', '3874345678', '2025-03-05', 'prodriguez', 'pass123', 'SOCIO'),
('Ana', 'Martínez', '45678901', 'ana.martinez@email.com', '3874456789', '2025-04-20', 'amartinez', 'pass123', 'SOCIO'),
('Luis', 'Fernández', '56789012', 'luis.fernandez@email.com', '3874567890', '2025-05-12', 'lfernandez', 'pass123', 'SOCIO'),
('Carlos', 'Díaz', '11111111', 'carlos.diaz@biblioteca.com', '3874111111', '2024-01-01', 'cdiaz', 'admin123', 'BIBLIOTECARIO'),
('Laura', 'López', '22222222', 'laura.lopez@biblioteca.com', '3874222222', '2024-01-01', 'llopez', 'admin123', 'BIBLIOTECARIO');

INSERT INTO Socio (id_usuario, numero_socio, fecha_vencimiento_carnet, estado, categoria) VALUES
(1, 'SOC-2025-001', '2025-12-31', 'ACTIVO'),
(2, 'SOC-2025-002', '2025-12-31', 'ACTIVO'),
(3, 'SOC-2025-003', '2025-12-31', 'ACTIVO'),
(4, 'SOC-2025-004', '2025-06-30', 'SUSPENDIDO'),
(5, 'SOC-2025-005', '2025-12-31', 'ACTIVO');

UPDATE Socio SET tiene_sanciones = TRUE WHERE id_socio = 4;

INSERT INTO Bibliotecario (id_usuario, legajo, turno) VALUES
(6, 'BIB-001', 'MAÑANA'),
(7, 'BIB-002', 'TARDE');

INSERT INTO PoliticaPrestamo (categoria, dias_prestamo, max_prestamos_simultaneos, multa_por_dia) VALUES
('GENERAL', 15, 3, 50.00),
('ESTUDIANTE', 21, 5, 30.00),
('DOCENTE', 30, 10, 25.00);

INSERT INTO Autor (nombre_completo, nacionalidad, fecha_nacimiento) VALUES
('Gabriel García Márquez', 'Colombiana', '1927-03-06'),
('Miguel de Cervantes', 'Española', '1547-09-29'),
('George Orwell', 'Británica', '1903-06-25'),
('Antoine de Saint-Exupéry', 'Francesa', '1900-06-29'),
('Julio Cortázar', 'Argentina', '1914-08-26');

INSERT INTO Editorial (nombre, pais) VALUES
('Editorial Sudamericana', 'Argentina'),
('Real Academia Española', 'España'),
('Secker & Warburg', 'Reino Unido'),
('Reynal & Hitchcock', 'Estados Unidos');

INSERT INTO Libro (titulo, id_autor, isbn, categoria, id_editorial, anio_publicacion) VALUES
('Cien Años de Soledad', 1, '978-0307474728', 'FICCION', 1, 1967),
('Don Quijote de la Mancha', 2, '978-8420412146', 'CLASICOS', 2, 1605),
('1984', 3, '978-0451524935', 'FICCION', 3, 1949),
('El Principito', 4, '978-0156012195', 'INFANTIL', 4, 1943),
('Rayuela', 5, '978-8420471891', 'FICCION', 1, 1963);

INSERT INTO Ejemplar (codigo_ejemplar, estado, ubicacion, id_libro) VALUES
('EJ-001-001', 'DISPONIBLE', 'Estante A1', 1),
('EJ-001-002', 'PRESTADO', 'Estante A1', 1),
('EJ-001-003', 'DISPONIBLE', 'Estante A1', 1),
('EJ-002-001', 'DISPONIBLE', 'Estante A2', 2),
('EJ-002-002', 'DISPONIBLE', 'Estante A2', 2),
('EJ-003-001', 'PRESTADO', 'Estante B1', 3),
('EJ-003-002', 'DISPONIBLE', 'Estante B1', 3),
('EJ-004-001', 'DISPONIBLE', 'Estante C1', 4),
('EJ-004-002', 'DISPONIBLE', 'Estante C1', 4),
('EJ-005-001', 'DISPONIBLE', 'Estante B2', 5);

INSERT INTO Prestamo (fecha_prestamo, fecha_vencimiento, estado, dias_prestamo, id_socio, id_ejemplar, id_bibliotecario) VALUES
('2025-09-20 10:30:00', '2025-10-05', 'ACTIVO', 15, 1, 2, 1),
('2025-09-25 14:00:00', '2025-10-10', 'ACTIVO', 15, 2, 6, 1),
('2025-09-01 09:00:00', '2025-09-16', 'VENCIDO', 15, 5, 1, 1);

INSERT INTO Devolucion (fecha_devolucion, estado_ejemplar, observaciones, multa, id_prestamo) VALUES
('2025-09-10 12:30:00', 'BUENO', 'Devolución con 6 días de atraso', 300.00, 3);

INSERT INTO Comprobante (fecha_emision, tipo, contenido, id_prestamo) VALUES
('2025-09-20 10:30:00', 'DIGITAL', 'Comprobante de préstamo - Libro: Cien Años de Soledad', 1),
('2025-09-25 14:00:00', 'DIGITAL', 'Comprobante de préstamo - Libro: 1984', 2);

INSERT INTO Notificacion (fecha_envio, tipo, mensaje, leida, id_prestamo) VALUES
('2025-10-03 08:00:00', 'RECORDATORIO', 'Su préstamo vence en 2 días', FALSE, 1),
('2025-09-17 08:00:00', 'ALERTA_ATRASO', 'Su préstamo está vencido', FALSE, 3);

INSERT INTO Historial (fecha, tipo_operacion, detalles, id_usuario, id_libro, id_prestamo) VALUES
('2025-09-20 10:30:00', 'PRESTAMO', 'Préstamo realizado', 1, 1, 1),
('2025-09-25 14:00:00', 'PRESTAMO', 'Préstamo realizado', 2, 3, 2),
('2025-09-10 12:30:00', 'DEVOLUCION', 'Devolución con multa', 5, 1, 3);

-- Consultas
-- Listar socios activos
SELECT s.numero_socio, u.nombre, u.apellido, u.email, s.estado
FROM Socio s
INNER JOIN Usuario u ON s.id_usuario = u.id_usuario
WHERE s.estado = 'ACTIVO';

-- Listar libros disponibles con nombre de autor
SELECT 
    l.titulo, 
    a.nombre_completo AS autor, 
    COUNT(e.id_ejemplar) AS total_disponibles
FROM Libro l
INNER JOIN Autor a ON l.id_autor = a.id_autor
INNER JOIN Ejemplar e ON l.id_libro = e.id_libro
WHERE e.estado = 'DISPONIBLE'
GROUP BY l.id_libro, l.titulo, a.nombre_completo;

-- Listar prestamos activos
SELECT p.id_prestamo, u.nombre, u.apellido, l.titulo, p.fecha_vencimiento
FROM Prestamo p
INNER JOIN Socio s ON p.id_socio = s.id_socio
INNER JOIN Usuario u ON s.id_usuario = u.id_usuario
INNER JOIN Ejemplar e ON p.id_ejemplar = e.id_ejemplar
INNER JOIN Libro l ON e.id_libro = l.id_libro
WHERE p.estado = 'ACTIVO';

-- Listar historial de un socio
SELECT h.fecha, h.tipo_operacion, l.titulo, h.detalles
FROM Historial h
INNER JOIN Libro l ON h.id_libro = l.id_libro
WHERE h.id_usuario = 1
ORDER BY h.fecha DESC;

-- Actualización y eliminación
-- Actualizar estado de un ejemplar
UPDATE Ejemplar
SET estado = 'PRESTADO'
WHERE codigo_ejemplar = 'EJ-001-001';

-- Marcar préstamos vencidos
SET SQL_SAFE_UPDATES = 0;
UPDATE Prestamo
SET estado = 'VENCIDO'
WHERE estado = 'ACTIVO' AND fecha_vencimiento < CURDATE();
SET SQL_SAFE_UPDATES = 1;

-- Eliminar notificaciones antiguas leidas
SET SQL_SAFE_UPDATES = 0;
DELETE FROM Notificacion
WHERE leida = TRUE AND fecha_envio < '2025-01-01';
SET SQL_SAFE_UPDATES = 1;

-- Casos de prueba
-- CP-CU03-01
-- 1. Verificar estado inicial del ejemplar
SELECT estado FROM Ejemplar WHERE codigo_ejemplar = 'EJ-003-002';

-- 2. Registrar préstamo válido
INSERT INTO Prestamo (fecha_prestamo, fecha_vencimiento, estado, dias_prestamo, 
                      id_socio, id_ejemplar, id_bibliotecario)
VALUES (NOW(), DATE_ADD(CURDATE(), INTERVAL 15 DAY), 'ACTIVO', 15, 1, 7, 1);

-- 3. Actualizar estado del ejemplar
UPDATE Ejemplar SET estado = 'PRESTADO' WHERE codigo_ejemplar = 'EJ-003-002';

-- 4. Insertar en historial
INSERT INTO Historial (fecha, tipo_operacion, detalles, id_usuario, id_libro, id_prestamo)
VALUES (NOW(), 'PRESTAMO', 'Préstamo registrado', 1, 3, LAST_INSERT_ID());

-- 5. Verificar estado final del ejemplar
SELECT estado FROM Ejemplar WHERE codigo_ejemplar = 'EJ-003-002';

-- CP-CU03-02
-- 1. Verificar estado del ejemplar
SELECT estado FROM Ejemplar WHERE codigo_ejemplar = 'EJ-001-002';

-- 2. Intentar validar disponibilidad
SELECT * FROM Ejemplar 
WHERE codigo_ejemplar = 'EJ-001-002' AND estado = 'DISPONIBLE';
-- Esperado: 0 filas → ejemplar no disponible

-- CP-CU03-03
-- 1. Verificar estado del socio
SELECT s.estado, s.tiene_sanciones, s.tiene_atrasos
FROM Socio s
WHERE s.id_socio = 4;

-- 2. Validar habilitación del socio
SELECT 
   CASE 
       WHEN s.estado = 'ACTIVO' 
        AND s.tiene_sanciones = FALSE 
        AND s.tiene_atrasos = FALSE 
       THEN 'HABILITADO'
       ELSE 'NO HABILITADO'
   END AS habilitacion
FROM Socio s
WHERE s.id_socio = 4;
-- Esperado: NO HABILITADO

-- CP-CU10-01
-- 1. Verificar datos del préstamo
SELECT id_prestamo, fecha_vencimiento, estado, id_ejemplar
FROM Prestamo
WHERE id_prestamo = 1;

-- 2. Calcular multa (no hay atraso)
SELECT 
   CASE 
       WHEN CURDATE() > fecha_vencimiento 
       THEN DATEDIFF(CURDATE(), fecha_vencimiento) * 50.00
       ELSE 0.00
   END AS multa
FROM Prestamo
WHERE id_prestamo = 1;

-- 3. Insertar devolución
INSERT INTO Devolucion (fecha_devolucion, estado_ejemplar, observaciones, multa, id_prestamo)
VALUES (NOW(), 'BUENO', 'Devolución en buen estado', 0.00, 1);

-- 4. Actualizar estado del ejemplar
UPDATE Ejemplar SET estado = 'DISPONIBLE' WHERE id_ejemplar = 2;

-- 5. Actualizar estado del préstamo
UPDATE Prestamo SET estado = 'DEVUELTO' WHERE id_prestamo = 1;

-- 6. Registrar en historial
INSERT INTO Historial (fecha, tipo_operacion, detalles, id_usuario, id_libro, id_prestamo)
VALUES (NOW(), 'DEVOLUCION', 'Devolución sin atraso', 1, 1, 1);

-- 7. Verificar estado del ejemplar luego de la devolución
SELECT id_ejemplar, codigo_ejemplar, estado
FROM Ejemplar
WHERE id_ejemplar = 2;

-- CP-CU10-02
-- precondicion
UPDATE Prestamo
SET fecha_vencimiento = '2025-09-16', estado = 'VENCIDO'
WHERE id_prestamo = 4;

-- 1. Verificar atraso
SELECT 
   id_prestamo,
   fecha_vencimiento,
   DATEDIFF(CURDATE(), fecha_vencimiento) AS dias_atraso
FROM Prestamo
WHERE id_prestamo = 4;

-- 2. Calcular multa
SELECT 
   DATEDIFF(CURDATE(), fecha_vencimiento) * 50.00 AS multa_calculada
FROM Prestamo
WHERE id_prestamo = 4;

-- 3. Insertar devolución con multa
INSERT INTO Devolucion (fecha_devolucion, estado_ejemplar, observaciones, multa, id_prestamo)
VALUES (NOW(), 'BUENO', 'Devolución con atraso de 16 días', 800.00, 4);

-- 4. Actualizar estado del ejemplar
UPDATE Ejemplar SET estado = 'DISPONIBLE' WHERE id_ejemplar = 2;

-- 5. Actualizar estado del préstamo
UPDATE Prestamo SET estado = 'DEVUELTO' WHERE id_prestamo = 4;

-- 6. Registrar en historial
INSERT INTO Historial (fecha, tipo_operacion, detalles, id_usuario, id_libro, id_prestamo)
VALUES (NOW(), 'DEVOLUCION', 'Devolución con multa de $800', 1, 1, 4);


select * from ejemplar;
select * from usuario;
select * from socio;
select * from prestamo;
select * from libro;