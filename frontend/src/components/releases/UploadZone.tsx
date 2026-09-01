'use client';

import React, { useState, useRef } from 'react';
import { useRouter } from 'next/navigation';
import { UploadMode } from '@/types/upload';
import { useUpload } from '@/hooks/useUpload';
import { Card, CardHeader, CardTitle, CardContent, CardFooter } from '@/components/ui/Card';
import Button from '@/components/ui/Button';
import Badge from '@/components/ui/Badge';
import ProgressBar from '@/components/ui/ProgressBar';
import {
  UploadCloud,
  FileArchive,
  CheckCircle2,
  AlertCircle,
  FolderTree,
  Package,
  ArrowRight,
  RefreshCw,
  X,
  FileCode2,
} from 'lucide-react';
import { formatBytes } from '@/lib/utils';

export interface UploadZoneProps {
  releaseId: string;
}

export const UploadZone: React.FC<UploadZoneProps> = ({ releaseId }) => {
  const router = useRouter();
  const [uploadMode, setUploadMode] = useState<UploadMode>('COMPLETE_PROJECT');
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [isDragOver, setIsDragOver] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const { uploadState, progress, error, setError, result, uploadProject, reset } = useUpload();

  const MAX_FILE_SIZE_BYTES = 50 * 1024 * 1024; // 50 MB

  const validateAndSelectFile = (file: File) => {
    setError(null);

    if (!file.name.toLowerCase().endsWith('.zip')) {
      setError('Invalid file format. Only .zip archive files are accepted.');
      return;
    }

    if (file.size > MAX_FILE_SIZE_BYTES) {
      setError(`File size (${formatBytes(file.size)}) exceeds maximum limit of 50 MB.`);
      return;
    }

    if (file.size === 0) {
      setError('The selected file is empty.');
      return;
    }

    setSelectedFile(file);
  };

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    setIsDragOver(false);
    if (uploadState === 'UPLOADING' || uploadState === 'EXTRACTING') return;

    if (e.dataTransfer.files && e.dataTransfer.files.length > 0) {
      validateAndSelectFile(e.dataTransfer.files[0]);
    }
  };

  const handleDragOver = (e: React.DragEvent) => {
    e.preventDefault();
    if (uploadState !== 'UPLOADING' && uploadState !== 'EXTRACTING') {
      setIsDragOver(true);
    }
  };

  const handleDragLeave = (e: React.DragEvent) => {
    e.preventDefault();
    setIsDragOver(false);
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files.length > 0) {
      validateAndSelectFile(e.target.files[0]);
    }
  };

  const handleStartUpload = async () => {
    if (!selectedFile || !releaseId) return;
    try {
      await uploadProject(releaseId, selectedFile, uploadMode);
    } catch {
      // Handled in hook state
    }
  };

  const handleCancel = () => {
    setSelectedFile(null);
    reset();
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
  };

  const isProcessing = uploadState === 'UPLOADING' || uploadState === 'EXTRACTING';

  return (
    <div className="space-y-6 max-w-2xl mx-auto">
      {/* Mode Selection Card */}
      <Card>
        <CardHeader className="pb-3">
          <CardTitle className="text-sm">How do you want to provide your project?</CardTitle>
          <p className="text-xs text-slate-500">
            Select the submission mode for this candidate release version.
          </p>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            {/* Mode 1: Complete Project */}
            <div
              onClick={() => !isProcessing && setUploadMode('COMPLETE_PROJECT')}
              className={`p-4 rounded-xl border-2 cursor-pointer transition flex flex-col justify-between space-y-2 ${
                uploadMode === 'COMPLETE_PROJECT'
                  ? 'border-indigo-600 bg-indigo-50/50 text-indigo-950 shadow-xs'
                  : 'border-slate-200 bg-white hover:border-slate-300 text-slate-700'
              }`}
            >
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <div className={`p-2 rounded-lg ${uploadMode === 'COMPLETE_PROJECT' ? 'bg-indigo-600 text-white' : 'bg-slate-100 text-slate-600'}`}>
                    <Package className="w-4 h-4" />
                  </div>
                  <span className="text-xs font-bold">Complete Project</span>
                </div>
                {uploadMode === 'COMPLETE_PROJECT' && (
                  <Badge variant="ready" size="sm">Selected</Badge>
                )}
              </div>
              <p className="text-[11px] text-slate-500 leading-relaxed">
                Upload the complete project as a single ZIP archive including source code, tests, and configuration.
              </p>
            </div>

            {/* Mode 2: Selected Files / Folders */}
            <div
              onClick={() => !isProcessing && setUploadMode('SELECTED_CONTENT')}
              className={`p-4 rounded-xl border-2 cursor-pointer transition flex flex-col justify-between space-y-2 ${
                uploadMode === 'SELECTED_CONTENT'
                  ? 'border-indigo-600 bg-indigo-50/50 text-indigo-950 shadow-xs'
                  : 'border-slate-200 bg-white hover:border-slate-300 text-slate-700'
              }`}
            >
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <div className={`p-2 rounded-lg ${uploadMode === 'SELECTED_CONTENT' ? 'bg-indigo-600 text-white' : 'bg-slate-100 text-slate-600'}`}>
                    <FolderTree className="w-4 h-4" />
                  </div>
                  <span className="text-xs font-bold">Selected Files / Folders</span>
                </div>
                {uploadMode === 'SELECTED_CONTENT' && (
                  <Badge variant="ready" size="sm">Selected</Badge>
                )}
              </div>
              <p className="text-[11px] text-slate-500 leading-relaxed">
                Upload only selected modules or subfolders (e.g. <code>src/</code>, <code>pom.xml</code>). Missing project components may limit evaluation.
              </p>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Upload Zone Card */}
      <Card>
        <CardHeader className="pb-3">
          <div className="flex items-center justify-between">
            <CardTitle className="text-sm">
              {uploadMode === 'COMPLETE_PROJECT'
                ? 'Upload Complete Project Archive'
                : 'Upload Selected Project Content'}
            </CardTitle>
            <span className="text-[11px] text-slate-400 font-mono">Max size: 50 MB</span>
          </div>
        </CardHeader>

        <CardContent className="space-y-4">
          {error && (
            <div className="p-3 rounded-xl bg-rose-50 border border-rose-200 text-rose-700 text-xs flex items-center justify-between gap-2">
              <div className="flex items-center gap-2">
                <AlertCircle className="w-4 h-4 shrink-0" />
                <span>{error}</span>
              </div>
              <button
                type="button"
                onClick={() => setError(null)}
                className="text-rose-500 hover:text-rose-800"
              >
                <X className="w-3.5 h-3.5" />
              </button>
            </div>
          )}

          {/* Success State */}
          {uploadState === 'READY' && result ? (
            <div className="p-6 rounded-2xl bg-emerald-50 border border-emerald-200 text-center space-y-4">
              <div className="w-12 h-12 rounded-2xl bg-emerald-600 text-white flex items-center justify-center mx-auto shadow-md">
                <CheckCircle2 className="w-6 h-6" />
              </div>
              <div className="space-y-1">
                <h3 className="text-sm font-bold text-emerald-950">Upload Successful & Validated</h3>
                <p className="text-xs text-emerald-700 max-w-md mx-auto">
                  Archive extracted safely into an isolated sandbox workspace. Code is treated strictly as data.
                </p>
              </div>

              <div className="p-3 bg-white rounded-xl border border-emerald-100 max-w-sm mx-auto text-left text-xs space-y-1.5 font-mono">
                <div className="flex justify-between">
                  <span className="text-slate-400">File:</span>
                  <span className="font-bold text-slate-800 truncate max-w-[200px]">{result.filename}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-slate-400">Size:</span>
                  <span className="text-slate-700">{formatBytes(result.fileSize)}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-slate-400">Extracted Files:</span>
                  <span className="text-slate-700 font-bold">{result.fileCount}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-slate-400">Mode:</span>
                  <span className="text-indigo-600 font-bold">{result.uploadMode}</span>
                </div>
              </div>

              <div className="pt-2 flex flex-col sm:flex-row items-center justify-center gap-3">
                <Button
                  variant="outline"
                  size="sm"
                  onClick={handleCancel}
                  leftIcon={<RefreshCw className="w-3.5 h-3.5" />}
                >
                  Upload Another Version
                </Button>
                <Button
                  size="sm"
                  onClick={() => router.push(`/releases/${releaseId}/analysis`)}
                  rightIcon={<ArrowRight className="w-3.5 h-3.5" />}
                >
                  Continue to Analysis Stage
                </Button>
              </div>
            </div>
          ) : (
            /* Dropzone Area */
            <div>
              <input
                ref={fileInputRef}
                type="file"
                accept=".zip,application/zip,application/x-zip-compressed"
                onChange={handleFileChange}
                disabled={isProcessing}
                className="hidden"
              />

              <div
                onDrop={handleDrop}
                onDragOver={handleDragOver}
                onDragLeave={handleDragLeave}
                onClick={() => !isProcessing && fileInputRef.current?.click()}
                className={`p-8 border-2 border-dashed rounded-2xl text-center cursor-pointer transition flex flex-col items-center justify-center space-y-3 ${
                  isDragOver
                    ? 'border-indigo-600 bg-indigo-50/70 scale-[0.99]'
                    : selectedFile
                    ? 'border-indigo-300 bg-indigo-50/20'
                    : 'border-slate-200 bg-slate-50/50 hover:bg-slate-50 hover:border-slate-300'
                }`}
              >
                <div className="w-12 h-12 rounded-2xl bg-indigo-50 text-indigo-600 flex items-center justify-center border border-indigo-100 shadow-xs">
                  <UploadCloud className="w-6 h-6" />
                </div>

                <div className="space-y-1">
                  <p className="text-xs font-bold text-slate-800">
                    Drag and drop your project ZIP here, or <span className="text-indigo-600 underline">Browse Files</span>
                  </p>
                  <p className="text-[11px] text-slate-400">
                    Supports .ZIP archives up to 50 MB
                  </p>
                </div>
              </div>

              {/* Selected File Card */}
              {selectedFile && uploadState !== 'READY' && (
                <div className="mt-4 p-3.5 rounded-xl border border-slate-200 bg-white flex items-center justify-between">
                  <div className="flex items-center gap-2.5">
                    <div className="p-2 rounded-lg bg-indigo-50 text-indigo-600">
                      <FileArchive className="w-4 h-4" />
                    </div>
                    <div>
                      <p className="text-xs font-bold text-slate-900 truncate max-w-xs">{selectedFile.name}</p>
                      <p className="text-[10px] text-slate-400 font-mono">{formatBytes(selectedFile.size)}</p>
                    </div>
                  </div>

                  {!isProcessing && (
                    <button
                      type="button"
                      onClick={(e) => {
                        e.stopPropagation();
                        handleCancel();
                      }}
                      className="p-1 rounded text-slate-400 hover:text-slate-600"
                    >
                      <X className="w-4 h-4" />
                    </button>
                  )}
                </div>
              )}

              {/* Progress State */}
              {isProcessing && (
                <div className="mt-4 p-4 rounded-xl bg-slate-50 border border-slate-200 space-y-2">
                  <div className="flex items-center justify-between text-xs font-semibold text-slate-700">
                    <span>
                      {uploadState === 'EXTRACTING'
                        ? 'Extracting and verifying sandbox workspace...'
                        : 'Uploading archive...'}
                    </span>
                    <span className="font-mono text-indigo-600">{progress}%</span>
                  </div>
                  <ProgressBar value={progress} variant="indigo" />
                </div>
              )}
            </div>
          )}
        </CardContent>

        {uploadState !== 'READY' && (
          <CardFooter className="justify-between border-t border-slate-100 pt-3">
            <Button
              variant="outline"
              size="sm"
              disabled={isProcessing}
              onClick={() => router.push(`/releases/${releaseId}`)}
            >
              Cancel
            </Button>
            <Button
              size="sm"
              loading={isProcessing}
              disabled={!selectedFile || isProcessing}
              onClick={handleStartUpload}
              leftIcon={<UploadCloud className="w-3.5 h-3.5" />}
            >
              {isProcessing ? 'Processing Archive...' : 'Upload & Validate'}
            </Button>
          </CardFooter>
        )}
      </Card>
    </div>
  );
};

export default UploadZone;
