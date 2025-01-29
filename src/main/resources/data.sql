INSERT INTO public.cars(
    year, brand, model)
VALUES
    (2011, 'toyota', 'yaris'),
    (2017, 'alfa romeo', '156')
;

INSERT INTO users (username, password, role) VALUES ('admin', '$2a$10$RTmX/Behh84N9N7yQCRwOuaFbg21V8SlfaWBYFd0g4Ko8I6tFuWfy', 'ADMIN');
INSERT INTO users (username, password, role) VALUES ('user', '$2a$10$RTmX/Behh84N9N7yQCRwOuaFbg21V8SlfaWBYFd0g4Ko8I6tFuWfy', 'USER');