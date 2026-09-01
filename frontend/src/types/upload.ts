export type UploadMode = 'COMPLETE_PROJECT' | 'SELECTED_CONTENT';

export type UploadState =
  | 'IDLE'
  | 'SELECTED'
  | 'UPLOADING'
  | 'VALIDATING'
  | 'EXTRACTING'
  | 'READY'
  | 'FAILED'
  | 'INVALID_FILE'
  | 'CANCELLED';

export interface UploadResponse {
  uploadId: string;
  releaseId: string;
  uploadMode: UploadMode;
  filename: string;
  fileSize: number;
  fileCount: number;
  status: string;
  uploadedAt: string;
  message?: string;
}
