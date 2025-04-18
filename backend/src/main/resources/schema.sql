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
