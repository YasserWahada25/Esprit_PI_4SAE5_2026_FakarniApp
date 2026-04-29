import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { tap, shareReplay } from 'rxjs/operators';
import {
  Group,
  CreateGroupRequest,
  UpdateGroupRequest,
  GroupMember,
  AddMemberRequest,
  UpdateMemberRoleRequest
} from '../models/group.model';

@Injectable({
  providedIn: 'root'
})
export class GroupService {
  // Utilise le proxy configuré dans proxy.conf.json
  // En production, remplacer par l'URL complète ou utiliser environment.apiUrl
  private apiUrl = '/api/groups';
  
  // Cache simple pour améliorer les performances
  private groupsCache$: Observable<Group[]> | null = null;
  private cacheTimestamp: number = 0;
  private readonly CACHE_DURATION = 30000; // 30 secondes

  constructor(private http: HttpClient) {}

  // Gestion des groupes
  createGroup(request: CreateGroupRequest): Observable<Group> {
    this.clearCache(); // Invalider le cache
    return this.http.post<Group>(this.apiUrl, request);
  }

  getGroupById(id: number, includeMembers: boolean = true): Observable<Group> {
    const params = new HttpParams().set('includeMembers', includeMembers.toString());
    // Ne pas utiliser le cache pour les détails d'un groupe spécifique
    return this.http.get<Group>(`${this.apiUrl}/${id}`, { params });
  }

  getAllGroups(includeMembers: boolean = false): Observable<Group[]> {
    // Utiliser le cache si disponible et valide
    const now = Date.now();
    if (this.groupsCache$ && (now - this.cacheTimestamp) < this.CACHE_DURATION) {
      return this.groupsCache$;
    }

    const params = new HttpParams().set('includeMembers', includeMembers.toString());
    this.groupsCache$ = this.http.get<Group[]>(this.apiUrl, { params }).pipe(
      shareReplay(1) // Partager le résultat entre plusieurs souscriptions
    );
    this.cacheTimestamp = now;
    
    return this.groupsCache$;
  }

  updateGroup(id: number, request: UpdateGroupRequest): Observable<Group> {
    this.clearCache();
    return this.http.put<Group>(`${this.apiUrl}/${id}`, request);
  }

  deleteGroup(id: number): Observable<void> {
    this.clearCache();
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  // Gestion des membres
  addMember(groupId: number, request: AddMemberRequest): Observable<GroupMember> {
    this.clearCache();
    return this.http.post<GroupMember>(`${this.apiUrl}/${groupId}/members`, request);
  }

  removeMember(groupId: number, userId: string): Observable<void> {
    this.clearCache();
    return this.http.delete<void>(`${this.apiUrl}/${groupId}/members/${userId}`);
  }

  updateMemberRole(groupId: number, userId: string, request: UpdateMemberRoleRequest): Observable<GroupMember> {
    return this.http.patch<GroupMember>(`${this.apiUrl}/${groupId}/members/${userId}/role`, request);
  }

  getGroupMembers(groupId: number): Observable<GroupMember[]> {
    return this.http.get<GroupMember[]>(`${this.apiUrl}/${groupId}/members`);
  }

  getUserGroups(userId: string): Observable<Group[]> {
    return this.http.get<Group[]>(`${this.apiUrl}/user/${userId}`);
  }

  // Méthode pour invalider le cache manuellement
  clearCache(): void {
    this.groupsCache$ = null;
    this.cacheTimestamp = 0;
  }
}
