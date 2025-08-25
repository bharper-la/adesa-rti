import http from 'k6/http';
import { check, sleep } from 'k6';
import { SharedArray } from 'k6/data';
import { randomItem } from 'https://jslib.k6.io/k6-utils/1.4.0/index.js';

const events = new SharedArray('events', () => {
    // Load one or more files; add more file names as needed
    return [open('../samples/vehicleAdded_v2.json'), open('../samples/vehicleUpdated_v2.json')];
});

export const options = {
    vus: 10,           // virtual users
    duration: '30s',   // test length
    thresholds: {
        http_req_duration: ['p(95)<300'], // 95% under 300ms
        http_req_failed: ['rate<0.01'],
    },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const SECRET   = __ENV.WEBHOOK_SECRET || 'secret';

export default function () {
    const body = randomItem(events); // random event each request
    const url  = `${BASE_URL}/events`;

    const params = {
        headers: {
            'Content-Type': 'application/cloudevents+json',
            'X-Webhook-Secret': SECRET,
        },
    };

    const res = http.post(url, body, params);

    check(res, {
        'status is 200': (r) => r.status === 200,
    });

    sleep(0.5);
}
