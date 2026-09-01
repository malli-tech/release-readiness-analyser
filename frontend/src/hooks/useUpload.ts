'use client';

import { useState } from 'react';
import { UploadMode, UploadState, UploadResponse } from '@/types/upload';
import { ApiError } from '@/lib/api';

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

export function useUpload() {
  const [uploadState, setUploadState] = useState<UploadState>('IDLE');
  const [progress, setProgress] = useState<number>(0);
  const [error, setError] = useState<string | null>(null);
  const [result, setResult] = useState<UploadResponse | null>(null);

  const uploadProject = async (
    releaseId: string,
    file: File,
    uploadMode: UploadMode = 'COMPLETE_PROJECT'
  ): Promise<UploadResponse> => {
    setError(null);
    setProgress(0);
    setUploadState('UPLOADING');

    return new Promise((resolve, reject) => {
      const xhr = new XMLHttpRequest();
      const formData = new FormData();
      formData.append('file', file);
      formData.append('uploadMode', uploadMode);

      xhr.upload.addEventListener('progress', (event) => {
        if (event.lengthComputable) {
          const percent = Math.round((event.loaded / event.total) * 90);
          setProgress(percent);
          if (percent >= 90) {
            setUploadState('EXTRACTING');
          }
        }
      });

      xhr.addEventListener('load', () => {
        if (xhr.status >= 200 && xhr.status < 300) {
          try {
            const data: UploadResponse = JSON.parse(xhr.responseText);
            setProgress(100);
            setUploadState('READY');
            setResult(data);
            resolve(data);
          } catch (e) {
            setUploadState('FAILED');
            setError('Failed to parse server response.');
            reject(new Error('Failed to parse server response.'));
          }
        } else {
          setUploadState('FAILED');
          try {
            const errData = JSON.parse(xhr.responseText);
            const errMsg = errData.message || 'Upload failed. Invalid archive contents.';
            setError(errMsg);
            reject(new Error(errMsg));
          } catch {
            const errMsg = xhr.status === 413
              ? 'File size exceeds maximum allowed upload limit (50 MB).'
              : `Upload failed with status ${xhr.status}`;
            setError(errMsg);
            reject(new Error(errMsg));
          }
        }
      });

      xhr.addEventListener('error', () => {
        setUploadState('FAILED');
        setError('Network error during file upload. Please check connection.');
        reject(new Error('Network error during upload.'));
      });

      xhr.addEventListener('abort', () => {
        setUploadState('CANCELLED');
        setError('Upload was cancelled.');
        reject(new Error('Upload cancelled.'));
      });

      const token = typeof window !== 'undefined' ? localStorage.getItem('auth_token') : null;

      xhr.open('POST', `${API_BASE_URL}/api/releases/${releaseId}/upload`);
      if (token) {
        xhr.setRequestHeader('Authorization', `Bearer ${token}`);
      }
      // Note: Do NOT set Content-Type header so browser sets multipart/form-data boundary
      xhr.send(formData);
    });
  };

  const reset = () => {
    setUploadState('IDLE');
    setProgress(0);
    setError(null);
    setResult(null);
  };

  return {
    uploadState,
    setUploadState,
    progress,
    error,
    setError,
    result,
    uploadProject,
    reset,
  };
}
