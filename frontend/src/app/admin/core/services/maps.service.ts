import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../../../../environments/environment';

export interface GeoResult {
    query: string;
    lat: number;
    lng: number;
    formattedAddress: string;
}

@Injectable({
    providedIn: 'root'
})
export class MapsService {
    private apiUrl = `${environment.apiBaseUrl}/api/maps/geocode`;

    constructor(private http: HttpClient) { }

    /**
     * Geocode a location text string into coordinates
     */
    geocode(query: string): Observable<GeoResult> {
        return this.http.get<GeoResult>(`${this.apiUrl}?query=${encodeURIComponent(query)}`);
    }
}
