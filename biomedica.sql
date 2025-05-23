--
-- PostgreSQL database dump
--

-- Dumped from database version 17.1
-- Dumped by pg_dump version 17.1

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: delete_stale_unpaid_orders(integer); Type: PROCEDURE; Schema: public; Owner: postgres
--

CREATE PROCEDURE public.delete_stale_unpaid_orders(IN days_threshold integer DEFAULT 7)
    LANGUAGE plpgsql
    AS $$
DECLARE
    deleted_count INTEGER;
BEGIN
    -- Delete unpaid orders that are older than the threshold
    DELETE FROM orders
    WHERE
        is_paid = FALSE AND
        order_date < (CURRENT_TIMESTAMP - (days_threshold || ' days')::INTERVAL);

    GET DIAGNOSTICS deleted_count = ROW_COUNT;

    -- Log the operation
    RAISE NOTICE 'Deleted % unpaid orders older than % days', deleted_count, days_threshold;
END;
$$;


ALTER PROCEDURE public.delete_stale_unpaid_orders(IN days_threshold integer) OWNER TO postgres;

--
-- Name: getlaboratorieswithpendingtests(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.getlaboratorieswithpendingtests() RETURNS TABLE(laboratory_id uuid, laboratory_address text, pending_tests_count integer)
    LANGUAGE plpgsql
    AS $$
BEGIN
    RETURN QUERY
    SELECT
        l.id AS laboratory_id,
        l.address::text AS laboratory_address,
        COUNT(tr.id)::integer AS pending_tests_count
    FROM laboratory l
    LEFT JOIN test_result tr ON l.id = tr.laboratory_id AND tr.result IS NULL
    GROUP BY l.id, l.address
    ORDER BY pending_tests_count DESC;
END;
$$;


ALTER FUNCTION public.getlaboratorieswithpendingtests() OWNER TO postgres;

--
-- Name: prevent_future_test_results(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.prevent_future_test_results() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    -- Check if test_date is in the future
    IF NEW.test_date > CURRENT_TIMESTAMP THEN
        IF NEW.result IS NOT NULL THEN
            RAISE EXCEPTION 'Cannot set test result for a future test date (%)!', NEW.test_date;
        END IF;
    END IF;

    -- If setting result, ensure test_date is provided and not in the future
    IF NEW.result IS NOT NULL AND OLD.result IS NULL THEN
        IF NEW.test_date IS NULL THEN
            RAISE EXCEPTION 'Test date is required when adding test results!';
        END IF;
    END IF;

    RETURN NEW;
END;
$$;


ALTER FUNCTION public.prevent_future_test_results() OWNER TO postgres;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: administrator; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.administrator (
    id uuid NOT NULL
);


ALTER TABLE public.administrator OWNER TO postgres;

--
-- Name: laboratory; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.laboratory (
    id uuid NOT NULL,
    address character varying(255),
    phone_number character varying(255),
    working_hours character varying(255)
);


ALTER TABLE public.laboratory OWNER TO postgres;

--
-- Name: laboratory_assistant; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.laboratory_assistant (
    phone_number character varying(255),
    id uuid NOT NULL,
    laboratory_id uuid
);


ALTER TABLE public.laboratory_assistant OWNER TO postgres;

--
-- Name: orders; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.orders (
    id uuid NOT NULL,
    is_paid boolean NOT NULL,
    order_date timestamp(6) with time zone,
    patient_id uuid
);


ALTER TABLE public.orders OWNER TO postgres;

--
-- Name: patient; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.patient (
    id uuid NOT NULL
);


ALTER TABLE public.patient OWNER TO postgres;

--
-- Name: test; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.test (
    id uuid NOT NULL,
    description character varying(255),
    name character varying(255),
    price double precision NOT NULL
);


ALTER TABLE public.test OWNER TO postgres;

--
-- Name: test_laboratory; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.test_laboratory (
    test_id uuid NOT NULL,
    laboratory_id uuid NOT NULL
);


ALTER TABLE public.test_laboratory OWNER TO postgres;

--
-- Name: test_result; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.test_result (
    id uuid NOT NULL,
    result character varying(255),
    test_date timestamp(6) with time zone,
    laboratory_id uuid,
    laboratory_assistant_id uuid,
    order_id uuid,
    test_id uuid NOT NULL
);


ALTER TABLE public.test_result OWNER TO postgres;

--
-- Name: token; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.token (
    id uuid NOT NULL,
    token character varying(255) NOT NULL,
    token_type character varying(255),
    user_id uuid,
    CONSTRAINT token_token_type_check CHECK (((token_type)::text = ANY ((ARRAY['ACCESS'::character varying, 'REFRESH'::character varying])::text[])))
);


ALTER TABLE public.token OWNER TO postgres;

--
-- Name: users; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.users (
    id uuid NOT NULL,
    email character varying(255) NOT NULL,
    is_verified boolean NOT NULL,
    name character varying(50) NOT NULL,
    password character varying(255) NOT NULL,
    role character varying(255) NOT NULL,
    surname character varying(50) NOT NULL,
    version bigint,
    CONSTRAINT users_role_check CHECK (((role)::text = ANY ((ARRAY['PATIENT'::character varying, 'LABORATORY_ASSISTANT'::character varying, 'ADMINISTRATOR'::character varying])::text[])))
);


ALTER TABLE public.users OWNER TO postgres;

--
-- Name: verification_entity; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.verification_entity (
    id uuid NOT NULL,
    code character varying(255),
    expiration_date timestamp(6) with time zone,
    verification_type smallint,
    user_id uuid,
    CONSTRAINT verification_entity_verification_type_check CHECK (((verification_type >= 0) AND (verification_type <= 1)))
);


ALTER TABLE public.verification_entity OWNER TO postgres;

--
-- Name: administrator administrator_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.administrator
    ADD CONSTRAINT administrator_pkey PRIMARY KEY (id);


--
-- Name: test idx_test_name; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.test
    ADD CONSTRAINT idx_test_name UNIQUE (name);


--
-- Name: token idx_token_token; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.token
    ADD CONSTRAINT idx_token_token UNIQUE (token);


--
-- Name: users idx_user_email; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT idx_user_email UNIQUE (email);


--
-- Name: laboratory_assistant laboratory_assistant_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.laboratory_assistant
    ADD CONSTRAINT laboratory_assistant_pkey PRIMARY KEY (id);


--
-- Name: laboratory laboratory_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.laboratory
    ADD CONSTRAINT laboratory_pkey PRIMARY KEY (id);


--
-- Name: orders orders_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.orders
    ADD CONSTRAINT orders_pkey PRIMARY KEY (id);


--
-- Name: patient patient_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.patient
    ADD CONSTRAINT patient_pkey PRIMARY KEY (id);


--
-- Name: test test_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.test
    ADD CONSTRAINT test_pkey PRIMARY KEY (id);


--
-- Name: test_result test_result_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.test_result
    ADD CONSTRAINT test_result_pkey PRIMARY KEY (id);


--
-- Name: token token_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.token
    ADD CONSTRAINT token_pkey PRIMARY KEY (id);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: verification_entity verification_entity_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.verification_entity
    ADD CONSTRAINT verification_entity_pkey PRIMARY KEY (id);


--
-- Name: idx_lab_assistant_laboratory; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_lab_assistant_laboratory ON public.laboratory_assistant USING btree (laboratory_id);


--
-- Name: idx_order_patient; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_order_patient ON public.orders USING btree (patient_id);


--
-- Name: idx_test_laboratory_lab; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_test_laboratory_lab ON public.test_laboratory USING btree (laboratory_id);


--
-- Name: idx_test_laboratory_test; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_test_laboratory_test ON public.test_laboratory USING btree (test_id);


--
-- Name: idx_token_user; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_token_user ON public.token USING btree (user_id);


--
-- Name: idx_verification_email_type; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_verification_email_type ON public.verification_entity USING btree (user_id, verification_type);


--
-- Name: idx_verification_user; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_verification_user ON public.verification_entity USING btree (user_id);


--
-- Name: orders trg_prevent_future_test_results; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER trg_prevent_future_test_results BEFORE INSERT OR UPDATE ON public.orders FOR EACH ROW EXECUTE FUNCTION public.prevent_future_test_results();


--
-- Name: test_result fk4aqr782b5j8qf039n3rxnx7o1; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.test_result
    ADD CONSTRAINT fk4aqr782b5j8qf039n3rxnx7o1 FOREIGN KEY (laboratory_id) REFERENCES public.laboratory(id) ON DELETE SET NULL;


--
-- Name: test_result fk6llopy8iygw9qgwtkncm5c8lm; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.test_result
    ADD CONSTRAINT fk6llopy8iygw9qgwtkncm5c8lm FOREIGN KEY (order_id) REFERENCES public.orders(id);


--
-- Name: test_laboratory fk8btnk1dwoxwpwwa9x887bc9yu; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.test_laboratory
    ADD CONSTRAINT fk8btnk1dwoxwpwwa9x887bc9yu FOREIGN KEY (test_id) REFERENCES public.test(id);


--
-- Name: test_laboratory fk95n1yk8e2x99d3sckhuese0a9; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.test_laboratory
    ADD CONSTRAINT fk95n1yk8e2x99d3sckhuese0a9 FOREIGN KEY (laboratory_id) REFERENCES public.laboratory(id);


--
-- Name: test_result fkef3e8k7fgvkj4mox0lxrkf8hh; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.test_result
    ADD CONSTRAINT fkef3e8k7fgvkj4mox0lxrkf8hh FOREIGN KEY (test_id) REFERENCES public.test(id);


--
-- Name: laboratory_assistant fkewaho8wj6swxnpxypmvwprqkj; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.laboratory_assistant
    ADD CONSTRAINT fkewaho8wj6swxnpxypmvwprqkj FOREIGN KEY (laboratory_id) REFERENCES public.laboratory(id) ON DELETE SET NULL;


--
-- Name: patient fkf0or75ex3abs31ottuqg8s301; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.patient
    ADD CONSTRAINT fkf0or75ex3abs31ottuqg8s301 FOREIGN KEY (id) REFERENCES public.users(id);


--
-- Name: orders fkgrc0y8p5nsfxonpp4xolxxglc; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.orders
    ADD CONSTRAINT fkgrc0y8p5nsfxonpp4xolxxglc FOREIGN KEY (patient_id) REFERENCES public.patient(id);


--
-- Name: administrator fkhhfc2dvk9qfv0xd9vdbhs5xs0; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.administrator
    ADD CONSTRAINT fkhhfc2dvk9qfv0xd9vdbhs5xs0 FOREIGN KEY (id) REFERENCES public.users(id);


--
-- Name: token fkj8rfw4x0wjjyibfqq566j4qng; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.token
    ADD CONSTRAINT fkj8rfw4x0wjjyibfqq566j4qng FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- Name: test_result fkm0vahl2m2g071iryn89dcr3pj; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.test_result
    ADD CONSTRAINT fkm0vahl2m2g071iryn89dcr3pj FOREIGN KEY (laboratory_assistant_id) REFERENCES public.laboratory_assistant(id) ON DELETE SET NULL;


--
-- Name: laboratory_assistant fkne6uymqxunxt89gxi9fww88j4; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.laboratory_assistant
    ADD CONSTRAINT fkne6uymqxunxt89gxi9fww88j4 FOREIGN KEY (id) REFERENCES public.users(id);


--
-- Name: verification_entity fkpp07dcc5ngq3up6ufxxttwhdk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.verification_entity
    ADD CONSTRAINT fkpp07dcc5ngq3up6ufxxttwhdk FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- PostgreSQL database dump complete
--

