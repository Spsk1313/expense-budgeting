import http from 'k6/http';
import { check } from 'k6';

export const options = {
    vus: 10,
    duration: '30s',
};

const BASE_URL = 'http://localhost:8080';

export default function () {
    const response = http.get(
        `${BASE_URL}/api/users/1/reports/summary?from=2024-01-01&to=2026-08-31`
    );

    check(response, {
        'status is 200': (r) => r.status === 200,
    });
}