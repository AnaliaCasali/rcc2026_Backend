--- Primero ejecutar InsertLocalidad.sql antes de correr este script en MySQL

INSERT INTO `rcc`.`usuarios` (`email`, `password`, `rol`, `nombre_completo`, `biografia`, `director`, `localidad_id`,
                              `fecha_registro`, `debaja`, `whatsapp`)
VALUES ('lucia.gomez@gmail.com',
        '$2a$12$LQv3c1yqSNWvklg041.v9u2jG1H7dF7F7F7F7F7F7F7F7F7F7F7F', -- password123
        'USUARIO',
        'Lucía Gómez',
        'Bailarina apasionada por las danzas folklóricas argentinas y la preservación de nuestras raíces culturales.',
        NULL,
        1,
        NOW(),
        0,
        NULL),

       ('ballet.norte@rcc.com',
        '$2a$12$LQv3c1yqSNWvklg041.v9u2jG1H7dF7F7F7F7F7F7F7F7F7F7F7F', -- password123
        'GRUPO',
        'Ballet Folclórico Norte',
        'Agrupación de danza tradicional con más de 15 años de trayectoria en los escenarios del Litoral.',
        'Carlos ''El Chango'' Benítez',
        1,
        NOW(),
        0,
        '+5493794123456'),

       ('cultura.formosa@gmail.com',
        '$2a$12$LQv3c1yqSNWvklg041.v9u2jG1H7dF7F7F7F7F7F7F7F7F7F7F7F', -- password123
        'ORGANIZADOR',
        'Dirección de Cultura Formoseña',
        'Organismo oficial dedicado a la promoción, difusión y apoyo de artistas y eventos culturales de la región.',
        'Mariana Acuña',
        2,
        NOW(),
        0,
        NULL),

       ('tomas.paredes@hotmail.com',
        '$2a$12$LQv3c1yqSNWvklg041.v9u2jG1H7dF7F7F7F7F7F7F7F7F7F7F7F', -- password123
        'USUARIO',
        'Tomás Paredes',
        'Músico, compositor y apasionado por la guitarra clásica y el folklore norteño.',
        NULL,
        5,
        NOW(),
        0,
        NULL),

       ('academia.auka@rcc.com',
        '$2a$12$LQv3c1yqSNWvklg041.v9u2jG1H7dF7F7F7F7F7F7F7F7F7F7F7F', -- password123
        'GRUPO',
        'Academia de Danzas Auka',
        'Espacio de formación integral en malambo, zamba y danzas estilizadas para todas las edades.',
        'Esteban Ruarte',
        8,
        NOW(),
        0,
        '+5493624987654'),

       ('peca.elalero@gmail.com',
        '$2a$12$LQv3c1yqSNWvklg041.v9u2jG1H7dF7F7F7F7F7F7F7F7F7F7F7F', -- password123
        'ORGANIZADOR',
        'Peña Folklórica El Alero',
        'Punto de encuentro tradicional para músicos, bailarines y amantes de la buena música en vivo.',
        'Romina Soria',
        9,
        NOW(),
        0,
        NULL),

       ('folklore.misioneros@gmail.com',
        '$2a$12$LQv3c1yqSNWvklg041.v9u2jG1H7dF7F7F7F7F7F7F7F7F7F7F7F', -- password123
        'GRUPO',
        'Herencia Misionera',
        'Conjunto de música del litoral y polcas paraguayas con proyección regional.',
        'Lucas Medina',
        3, -- Posadas
        NOW(),
        0,
        '+5493764556677'),

       ('teatro.mendoza@mendoza.gob.ar',
        '$2a$12$LQv3c1yqSNWvklg041.v9u2jG1H7dF7F7F7F7F7F7F7F7F7F7F7F', -- password123
        'ORGANIZADOR',
        'Secretaría de Cultura de Mendoza',
        'Fomento de las artes escénicas y organización de los principales festivales provinciales de Cuyo.',
        'Sofía Valenzuela',
        4, -- Mendoza
        NOW(),
        0,
        NULL),

       ('mateo.gimenez@hotmail.com',
        '$2a$12$LQv3c1yqSNWvklg041.v9u2jG1H7dF7F7F7F7F7F7F7F7F7F7F7F', -- password123
        'USUARIO',
        'Mateo Giménez',
        'Bailarín independiente de malambo norteño y sureño.',
        NULL,
        6, -- San Salvador de Jujuy
        NOW(),
        0,
        NULL),

       ('tucuman.folklore@gmail.com',
        '$2a$12$LQv3c1yqSNWvklg041.v9u2jG1H7dF7F7F7F7F7F7F7F7F7F7F7F', -- password123
        'GRUPO',
        'Los Vidaleros de Tucumán',
        'Grupo de zambas y chacareras con amplia trayectoria en los festivales del NOA.',
        'Pedro Vidal',
        7, -- San Miguel de Tucumán
        NOW(),
        0,
        '+5493814112233'),

       ('asoc.lapampa@gmail.com',
        '$2a$12$LQv3c1yqSNWvklg041.v9u2jG1H7dF7F7F7F7F7F7F7F7F7F7F7F', -- password123
        'ORGANIZADOR',
        'Asociación Pampeana de Folklore',
        'Organización de encuentros de danza y peñas tradicionales en la pampa húmeda.',
        'Martín Echeverría',
        10, -- Santa Rosa
        NOW(),
        0,
        NULL),

       ('valeria.sosa@gmail.com',
        '$2a$12$LQv3c1yqSNWvklg041.v9u2jG1H7dF7F7F7F7F7F7F7F7F7F7F7F', -- password123
        'cUSUARIO',
        'Valeria Sosa',
        'Amante de la cultura tradicional y fotógrafa especializada en eventos folklóricos.',
        NULL,
        11, -- Corrientes
        NOW(),
        0,
        NULL);