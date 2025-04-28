CREATE OR REPLACE FUNCTION GetLaboratoriesWithPendingTests()
RETURNS TABLE (
    laboratory_id UUID,
    laboratory_address TEXT,
    pending_tests_count INTEGER
)
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

-- Prevent setting results for future test dates
CREATE OR REPLACE FUNCTION prevent_future_test_results()
RETURNS TRIGGER AS $$
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
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_prevent_future_test_results
BEFORE INSERT OR UPDATE ON orders
FOR EACH ROW
EXECUTE FUNCTION prevent_future_test_results();

-- Delete stale unpaid orders
CREATE OR REPLACE PROCEDURE delete_stale_unpaid_orders(days_threshold INTEGER DEFAULT 7)
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