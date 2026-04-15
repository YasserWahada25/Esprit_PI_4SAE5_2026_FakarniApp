export enum GroupType {
  PUBLIC = 'PUBLIC',
  PRIVATE = 'PRIVATE'
}

export enum GroupStatus {
  ACTIVE = 'ACTIVE',
  ARCHIVED = 'ARCHIVED',
  SUSPENDED = 'SUSPENDED'
}

export enum MemberRole {
  ADMIN = 'ADMIN',
  MODERATOR = 'MODERATOR',
  MEMBER = 'MEMBER'
}

export interface GroupMember {
  id: number;
  userId: number;
  role: MemberRole;
  joinedAt: string;
  invitedBy?: number;
}

export interface Group {
  id: number;
  name: string;
  description: string;
  creatorId: number;
  groupType: GroupType;
  status: GroupStatus;
  coverImageUrl?: string;
  maxMembers?: number;
  isJoinable: boolean;
  memberCount: number;
  members?: GroupMember[];
  createdAt: string;
  updatedAt: string;
}

export interface CreateGroupRequest {
  name: string;
  description: string;
  creatorId: number;
  groupType?: GroupType;
  coverImageUrl?: string;
  maxMembers?: number;
  isJoinable?: boolean;
}

export interface UpdateGroupRequest {
  name: string;
  description: string;
  groupType?: GroupType;
  coverImageUrl?: string;
  maxMembers?: number;
  isJoinable?: boolean;
}

export interface AddMemberRequest {
  userId: number;
  role?: MemberRole;
  invitedBy?: number;
}

export interface UpdateMemberRoleRequest {
  role: MemberRole;
}
